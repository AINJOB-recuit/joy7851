package com.ainjob.dto;

public class PendingApplicantResponse {
    private Long applicationId;
    private Long applicantId;
    private String applicantName;
    private String email;
    private Long stageId;
    private String stageName;
    private Long statusId;
    private String statusCode;
    private String statusName;
    private Long jobPostingId;
    private String jobTitle;

    public PendingApplicantResponse(Long applicationId, Long applicantId, String applicantName, String email,
                                    Long stageId, String stageName, Long statusId, String statusCode,
                                    String statusName, Long jobPostingId, String jobTitle) {
        this.applicationId = applicationId;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.email = email;
        this.stageId = stageId;
        this.stageName = stageName;
        this.statusId = statusId;
        this.statusCode = statusCode;
        this.statusName = statusName;
        this.jobPostingId = jobPostingId;
        this.jobTitle = jobTitle;
    }

    public Long getApplicationId() { return applicationId; }
    public Long getApplicantId() { return applicantId; }
    public String getApplicantName() { return applicantName; }
    public String getEmail() { return email; }
    public Long getStageId() { return stageId; }
    public String getStageName() { return stageName; }
    public Long getStatusId() { return statusId; }
    public String getStatusCode() { return statusCode; }
    public String getStatusName() { return statusName; }
    public Long getJobPostingId() { return jobPostingId; }
    public String getJobTitle() { return jobTitle; }
}
