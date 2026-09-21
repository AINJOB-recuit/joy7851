package com.ainjob.notification;

import com.ainjob.domain.Applicant;
import com.ainjob.domain.Stage;
import com.ainjob.domain.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class StageChangeNotifier {

    private static final Logger log = LoggerFactory.getLogger(StageChangeNotifier.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String from;

    public StageChangeNotifier(JavaMailSender mailSender,
                               @Value("${ainjob.mail.enabled:false}") boolean mailEnabled,
                               @Value("${ainjob.mail.from:noreply@ainjob.local}") String from) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.from = from;
    }

    public void notifyStageChanged(Applicant applicant, Stage fromStage, Stage toStage,
                                   Status fromStatus, Status toStatus) {
        String subject = "[AINJOB] 지원 단계가 변경되었습니다";
        String body = String.format(
                "안녕하세요 %s님,%n%n지원 상태가 변경되었습니다.%n" +
                        "- Stage: %s → %s%n- Status: %s → %s%n%n감사합니다.%nAINJOB ATS",
                applicant.getName(),
                fromStage.getName(), toStage.getName(),
                fromStatus.getName(), toStatus.getName()
        );

        if (!mailEnabled) {
            log.info("[MAIL-SKIPPED] to={} subject={}", applicant.getEmail(), subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(applicant.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[MAIL-SENT] to={} from={}", applicant.getEmail(), from);
        } catch (MailException ex) {
            // 전이 트랜잭션은 유지. 메일 실패는 로그만.
            log.error("[MAIL-FAILED] to={} reason={}", applicant.getEmail(), ex.getMessage());
        }
    }
}
