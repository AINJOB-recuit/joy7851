package com.ainjob.dto;

import javax.validation.constraints.NotNull;

public class TransitionRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long targetStageId;

    @NotNull
    private Long targetStatusId;

    private String reason;
    private String changedBy;

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getTargetStageId() { return targetStageId; }
    public void setTargetStageId(Long targetStageId) { this.targetStageId = targetStageId; }
    public Long getTargetStatusId() { return targetStatusId; }
    public void setTargetStatusId(Long targetStatusId) { this.targetStatusId = targetStatusId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
}
