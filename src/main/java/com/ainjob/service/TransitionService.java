package com.ainjob.service;

import com.ainjob.domain.*;
import com.ainjob.dto.TransitionRequest;
import com.ainjob.dto.TransitionResponse;
import com.ainjob.exception.ApiException;
import com.ainjob.notification.StageChangeNotifier;
import com.ainjob.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransitionService {

    private final ApplicationRepository applicationRepository;
    private final StageRepository stageRepository;
    private final StatusRepository statusRepository;
    private final AppHistoryRepository appHistoryRepository;
    private final ApplicantRepository applicantRepository;
    private final StageChangeNotifier stageChangeNotifier;

    public TransitionService(ApplicationRepository applicationRepository,
                             StageRepository stageRepository,
                             StatusRepository statusRepository,
                             AppHistoryRepository appHistoryRepository,
                             ApplicantRepository applicantRepository,
                             StageChangeNotifier stageChangeNotifier) {
        this.applicationRepository = applicationRepository;
        this.stageRepository = stageRepository;
        this.statusRepository = statusRepository;
        this.appHistoryRepository = appHistoryRepository;
        this.applicantRepository = applicantRepository;
        this.stageChangeNotifier = stageChangeNotifier;
    }

    @Transactional
    public TransitionResponse transition(Long applicationId, TransitionRequest request) {
        if (request.getCompanyId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "MISSING_COMPANY_ID",
                    "companyId is required");
        }

        ApplicationEntity app = applicationRepository
                .findByIdAndCompanyId(applicationId, request.getCompanyId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "NOT_FOUND",
                        "Application not found for company"));

        Stage currentStage = stageRepository.findById(app.getStageId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "STAGE_NOT_FOUND",
                        "Current stage not found"));
        Status currentStatus = statusRepository.findById(app.getStatusId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "STATUS_NOT_FOUND",
                        "Current status not found"));

        Stage targetStage = stageRepository.findByIdAndCompanyId(request.getTargetStageId(), request.getCompanyId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "TARGET_STAGE_NOT_FOUND",
                        "Target stage not found in company"));
        Status targetStatus = statusRepository.findById(request.getTargetStatusId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "TARGET_STATUS_NOT_FOUND",
                        "Target status not found"));

        validateTransition(currentStage, currentStatus, targetStage, targetStatus);

        Long fromStageId = app.getStageId();
        Long fromStatusId = app.getStatusId();
        app.setStageId(targetStage.getId());
        app.setStatusId(targetStatus.getId());
        applicationRepository.save(app);

        appHistoryRepository.save(AppHistory.of(
                app.getId(), fromStageId, targetStage.getId(),
                fromStatusId, targetStatus.getId(),
                request.getReason(), request.getChangedBy()
        ));

        applicantRepository.findById(app.getApplicantId()).ifPresent(applicant ->
                stageChangeNotifier.notifyStageChanged(applicant, currentStage, targetStage, currentStatus, targetStatus)
        );

        return new TransitionResponse(
                app.getId(), app.getCompanyId(),
                targetStage.getId(), targetStage.getName(),
                targetStatus.getId(), targetStatus.getCode(), targetStatus.getName()
        );
    }

    /**
     * Stage 축과 Status 축을 분리한 전이 규칙.
     * 잘못된 전이는 409 Conflict.
     */
    private void validateTransition(Stage fromStage, Status fromStatus, Stage toStage, Status toStatus) {
        String fromStatusCode = fromStatus.getCode();
        String toStatusCode = toStatus.getCode();
        int fromOrder = fromStage.getSortOrder();
        int toOrder = toStage.getSortOrder();

        // 동일 stage/status 는 no-op 금지 → 409
        if (fromStage.getId().equals(toStage.getId()) && fromStatus.getId().equals(toStatus.getId())) {
            throw conflict("동일 Stage/Status 로의 전이는 허용하지 않습니다.");
        }

        // 불합격/최종합격(합격) 이후 임의 점프 제한
        if ("REJECTED".equals(fromStatusCode) && !"IN_PROGRESS".equals(toStatusCode)) {
            // 불합격 → 진행중(재심사)만 허용 (같은 단계 복구)
            if (!(fromOrder == toOrder && "IN_PROGRESS".equals(toStatusCode))) {
                throw conflict("불합격 상태는 동일 Stage에서 진행중으로만 복구할 수 있습니다.");
            }
        }

        // 보류(Pending) → 진행중 복구: 동일 Stage 내에서만 가능
        if ("PENDING".equals(fromStatusCode) && "IN_PROGRESS".equals(toStatusCode)) {
            if (fromOrder != toOrder) {
                throw conflict("보류→진행중 복구는 동일 Stage 내에서만 가능합니다.");
            }
            return;
        }

        // 진행중 → 보류: 동일 Stage
        if ("IN_PROGRESS".equals(fromStatusCode) && "PENDING".equals(toStatusCode)) {
            if (fromOrder != toOrder) {
                throw conflict("보류 처리는 동일 Stage 내에서만 가능합니다.");
            }
            return;
        }

        // 단계 전진: 현재 Status가 합격이어야 다음 Stage로 이동, 다음 Stage는 진행중으로 시작
        if (toOrder == fromOrder + 1) {
            if (!"PASSED".equals(fromStatusCode)) {
                throw conflict("다음 Stage로 이동하려면 현재 Status가 합격이어야 합니다.");
            }
            if (!"IN_PROGRESS".equals(toStatusCode) && !"PASSED".equals(toStatusCode)) {
                // 최종합격 stage로 바로 합격 처리 허용
                throw conflict("다음 Stage 진입 시 Status는 진행중 또는 합격이어야 합니다.");
            }
            return;
        }

        // 동일 Stage 내 합격/불합격 판정
        if (toOrder == fromOrder) {
            if ("PENDING".equals(fromStatusCode) && ("PASSED".equals(toStatusCode) || "REJECTED".equals(toStatusCode))) {
                throw conflict("보류 상태에서는 먼저 진행중으로 복구한 뒤 합격/불합격을 처리해야 합니다.");
            }
            if ("IN_PROGRESS".equals(fromStatusCode)
                    && ("PASSED".equals(toStatusCode) || "REJECTED".equals(toStatusCode) || "PENDING".equals(toStatusCode))) {
                return;
            }
            if ("PASSED".equals(fromStatusCode) && "IN_PROGRESS".equals(toStatusCode)) {
                // 합격 취소(동일 Stage)
                return;
            }
            throw conflict("허용되지 않는 동일 Stage 상태 전이입니다.");
        }

        // 단계 취소(1단계 후퇴): 다음 Stage 진행중/보류 → 이전 Stage 합격으로 복구
        if (toOrder == fromOrder - 1) {
            if (!("IN_PROGRESS".equals(fromStatusCode) || "PENDING".equals(fromStatusCode))) {
                throw conflict("Stage 후퇴는 현재 단계가 진행중/보류일 때만 가능합니다.");
            }
            if (!"PASSED".equals(toStatusCode)) {
                throw conflict("Stage 후퇴 시 이전 Stage Status는 합격이어야 합니다.");
            }
            return;
        }

        throw conflict("Stage는 한 단계씩만 전진/후퇴할 수 있습니다. (현재 sortOrder="
                + fromOrder + " → " + toOrder + ")");
    }

    private ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT.value(), "INVALID_TRANSITION", message);
    }
}
