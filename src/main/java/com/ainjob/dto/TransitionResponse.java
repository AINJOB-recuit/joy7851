package com.ainjob.dto;

public class TransitionResponse {
    private Long applicationId;
    private Long companyId;
    private Long stageId;
    private String stageName;
    private Long statusId;
    private String statusCode;
    private String statusName;

    public TransitionResponse(Long applicationId, Long companyId, Long stageId, String stageName,
                              Long statusId, String statusCode, String statusName) {
        this.applicationId = applicationId;
        this.companyId = companyId;
        this.stageId = stageId;
        this.stageName = stageName;
        this.statusId = statusId;
        this.statusCode = statusCode;
        this.statusName = statusName;
    }

    public Long getApplicationId() { return applicationId; }
    public Long getCompanyId() { return companyId; }
    public Long getStageId() { return stageId; }
    public String getStageName() { return stageName; }
    public Long getStatusId() { return statusId; }
    public String getStatusCode() { return statusCode; }
    public String getStatusName() { return statusName; }
}
