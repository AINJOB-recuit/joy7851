package com.ainjob.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 조건 충족 지원자 필터.
 * companyId / positionCode 필수.
 * stageSortOrder / statusCode 는 선택 — 있으면 해당 stage×status 로 분류 필터.
 */
public class QualifiedFilterRequest {

    private Long companyId;
    private String positionCode;
    private Double minYears;
    private List<String> skillKeys = new ArrayList<>();
    private Boolean requireCsRelatedBachelor = true;
    private Integer degreeSortOrder = 1;
    /** null 이면 모든 stage */
    private Integer stageSortOrder;
    /** null/blank 이면 모든 status */
    private String statusCode;
    private Long jobPostingId;

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getPositionCode() { return positionCode; }
    public void setPositionCode(String positionCode) { this.positionCode = positionCode; }

    public Double getMinYears() { return minYears; }
    public void setMinYears(Double minYears) { this.minYears = minYears; }

    public List<String> getSkillKeys() { return skillKeys; }
    public void setSkillKeys(List<String> skillKeys) {
        this.skillKeys = skillKeys == null ? new ArrayList<>() : skillKeys;
    }

    public Boolean getRequireCsRelatedBachelor() { return requireCsRelatedBachelor; }
    public void setRequireCsRelatedBachelor(Boolean requireCsRelatedBachelor) {
        this.requireCsRelatedBachelor = requireCsRelatedBachelor;
    }

    public Integer getDegreeSortOrder() { return degreeSortOrder; }
    public void setDegreeSortOrder(Integer degreeSortOrder) { this.degreeSortOrder = degreeSortOrder; }

    public Integer getStageSortOrder() { return stageSortOrder; }
    public void setStageSortOrder(Integer stageSortOrder) { this.stageSortOrder = stageSortOrder; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public Long getJobPostingId() { return jobPostingId; }
    public void setJobPostingId(Long jobPostingId) { this.jobPostingId = jobPostingId; }

    public List<String> normalizedSkillKeys() {
        if (skillKeys == null || skillKeys.isEmpty()) {
            return Collections.emptyList();
        }
        return skillKeys.stream()
                .filter(s -> s != null && !s.isBlank())
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.toList());
    }
}
