package com.ainjob.domain;

import javax.persistence.*;

@Entity
@Table(name = "application")
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "job_posting_id", nullable = false)
    private Long jobPostingId;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "stage_id", nullable = false)
    private Long stageId;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public Long getJobPostingId() { return jobPostingId; }
    public Long getApplicantId() { return applicantId; }
    public Long getStageId() { return stageId; }
    public Long getStatusId() { return statusId; }

    public void setStageId(Long stageId) { this.stageId = stageId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }
}
