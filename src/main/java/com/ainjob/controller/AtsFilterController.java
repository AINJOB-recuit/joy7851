package com.ainjob.controller;

import com.ainjob.dto.PendingApplicantResponse;
import com.ainjob.dto.QualifiedApplicantResponse;
import com.ainjob.dto.QualifiedFilterRequest;
import com.ainjob.service.AtsFilterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ats")
public class AtsFilterController {

    private final AtsFilterService atsFilterService;

    public AtsFilterController(AtsFilterService atsFilterService) {
        this.atsFilterService = atsFilterService;
    }

    /**
     * 조건 충족 지원자.
     * stageSortOrder / statusCode 는 선택 — 있으면 해당 stage×status 로 분류.
     */
    @GetMapping("/qualified")
    public List<QualifiedApplicantResponse> qualified(
            @RequestParam Long companyId,
            @RequestParam String positionCode,
            @RequestParam(required = false) Double minYears,
            @RequestParam(required = false) List<String> skillKeys,
            @RequestParam(required = false, defaultValue = "true") Boolean requireCsRelatedBachelor,
            @RequestParam(required = false, defaultValue = "1") Integer degreeSortOrder,
            @RequestParam(required = false) Integer stageSortOrder,
            @RequestParam(required = false) String statusCode,
            @RequestParam(required = false) Long jobPostingId) {

        QualifiedFilterRequest filter = new QualifiedFilterRequest();
        filter.setCompanyId(companyId);
        filter.setPositionCode(positionCode);
        filter.setMinYears(minYears);
        filter.setSkillKeys(skillKeys);
        filter.setRequireCsRelatedBachelor(requireCsRelatedBachelor);
        filter.setDegreeSortOrder(degreeSortOrder);
        filter.setStageSortOrder(stageSortOrder);
        filter.setStatusCode(statusCode);
        filter.setJobPostingId(jobPostingId);
        return atsFilterService.findQualified(filter);
    }

    /**
     * stage / status 별 분류.
     * 예) stageSortOrder=2&statusCode=PENDING
     *     stageSortOrder=3 (해당 stage 전체 status)
     *     statusCode=PASSED (모든 stage 의 PASSED)
     *     companyId 만 → 전사 지원 목록
     */
    @GetMapping("/applications")
    public List<PendingApplicantResponse> applications(
            @RequestParam Long companyId,
            @RequestParam(required = false) Integer stageSortOrder,
            @RequestParam(required = false) String stageName,
            @RequestParam(required = false) String statusCode) {
        return atsFilterService.findByStageAndStatus(companyId, stageSortOrder, stageName, statusCode);
    }
}
