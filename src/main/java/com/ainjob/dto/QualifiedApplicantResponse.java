package com.ainjob.dto;

import java.math.BigDecimal;

public class QualifiedApplicantResponse {
    private Long applicationId;
    private Long applicantId;
    private String applicantName;
    private String email;
    private BigDecimal experienceYears;
    private Long stageId;
    private String stageName;
    private Integer stageSortOrder;
    private Long statusId;
    private String statusCode;
    private String statusName;
    private String positionCode;

    public QualifiedApplicantResponse(Long applicationId, Long applicantId, String applicantName,
                                      String email, BigDecimal experienceYears,
                                      Long stageId, String stageName, Integer stageSortOrder,
                                      Long statusId, String statusCode, String statusName,
                                      String positionCode) {
        this.applicationId = applicationId;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.email = email;
        this.experienceYears = experienceYears;
        this.stageId = stageId;
        this.stageName = stageName;
        this.stageSortOrder = stageSortOrder;
        this.statusId = statusId;
        this.statusCode = statusCode;
        this.statusName = statusName;
        this.positionCode = positionCode;
    }

    public Long getApplicationId() { return applicationId; }
    public Long getApplicantId() { return applicantId; }
    public String getApplicantName() { return applicantName; }
    public String getEmail() { return email; }
    public BigDecimal getExperienceYears() { return experienceYears; }
    public Long getStageId() { return stageId; }
    public String getStageName() { return stageName; }
    public Integer getStageSortOrder() { return stageSortOrder; }
    public Long getStatusId() { return statusId; }
    public String getStatusCode() { return statusCode; }
    public String getStatusName() { return statusName; }
    public String getPositionCode() { return positionCode; }
}
