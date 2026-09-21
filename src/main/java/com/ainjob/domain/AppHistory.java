package com.ainjob.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_history")
public class AppHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "from_stage_id")
    private Long fromStageId;

    @Column(name = "to_stage_id", nullable = false)
    private Long toStageId;

    @Column(name = "from_status_id")
    private Long fromStatusId;

    @Column(name = "to_status_id", nullable = false)
    private Long toStatusId;

    private String reason;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    public static AppHistory of(Long applicationId, Long fromStageId, Long toStageId,
                                Long fromStatusId, Long toStatusId, String reason, String changedBy) {
        AppHistory h = new AppHistory();
        h.applicationId = applicationId;
        h.fromStageId = fromStageId;
        h.toStageId = toStageId;
        h.fromStatusId = fromStatusId;
        h.toStatusId = toStatusId;
        h.reason = reason;
        h.changedBy = changedBy;
        return h;
    }

    public Long getId() { return id; }
}
