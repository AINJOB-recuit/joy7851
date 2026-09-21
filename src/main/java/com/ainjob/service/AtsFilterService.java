package com.ainjob.service;

import com.ainjob.dto.PendingApplicantResponse;
import com.ainjob.dto.QualifiedApplicantResponse;
import com.ainjob.dto.QualifiedFilterRequest;
import com.ainjob.exception.ApiException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AtsFilterService {

    private final JdbcTemplate jdbcTemplate;

    public AtsFilterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 학력·경력·스킬 조건 충족 지원자.
     * stageSortOrder / statusCode 가 있으면 해당 stage×status 로 추가 분류.
     */
    public List<QualifiedApplicantResponse> findQualified(QualifiedFilterRequest filter) {
        if (filter.getCompanyId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "MISSING_COMPANY_ID",
                    "companyId is required");
        }
        Long companyId = filter.getCompanyId();
        if (filter.getPositionCode() == null || filter.getPositionCode().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "MISSING_POSITION",
                    "positionCode is required (BACKEND|FRONTEND)");
        }

        String positionCode = filter.getPositionCode().trim().toUpperCase();
        double minYears = resolveMinYears(filter, positionCode);
        boolean requireCs = filter.getRequireCsRelatedBachelor() == null
                || Boolean.TRUE.equals(filter.getRequireCsRelatedBachelor());
        int degreeSortOrder = filter.getDegreeSortOrder() == null ? 1 : filter.getDegreeSortOrder();

        Long positionId = requireOne(
                "SELECT id FROM position_type WHERE code = ?",
                "positionCode not found: " + positionCode,
                positionCode);
        Long degreeId = requireOne(
                "SELECT id FROM degree WHERE sort_order = ?",
                "degreeSortOrder not found: " + degreeSortOrder,
                degreeSortOrder);

        Long stageId = null;
        if (filter.getStageSortOrder() != null) {
            stageId = resolveStageId(companyId, filter.getStageSortOrder());
        }

        Long statusId = null;
        if (filter.getStatusCode() != null && !filter.getStatusCode().isBlank()) {
            String statusCode = filter.getStatusCode().trim().toUpperCase();
            statusId = requireOne(
                    "SELECT id FROM status WHERE code = ?",
                    "statusCode not found: " + statusCode,
                    statusCode);
        }

        List<Long> skillIds = resolveSkillIds(companyId, positionId, filter);
        if (skillIds.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "MISSING_SKILLS",
                    "skillKeys is empty and no job_posting_skill found for position");
        }

        String skillIn = skillIds.stream().map(id -> "?").collect(Collectors.joining(","));

        /*
         * 목적: company+공고 선필터 → 학력/경력/스킬. stage/status 는 선택.
         * 인덱스: idx_application_company_job_stage_status
         *         idx_application_company_stage_status_job_applicant (분류 시)
         */
        StringBuilder cand = new StringBuilder();
        cand.append("SELECT a.id AS application_id, a.applicant_id, a.stage_id, a.status_id, a.job_posting_id ");
        cand.append("FROM application a ");
        cand.append("JOIN job_posting jp ON jp.id = a.job_posting_id ");
        cand.append("WHERE a.company_id = ? ");
        cand.append("  AND jp.company_id = a.company_id ");
        cand.append("  AND jp.position_type_id = ? ");
        if (filter.getJobPostingId() != null) {
            cand.append("  AND a.job_posting_id = ? ");
        }
        if (stageId != null) {
            cand.append("  AND a.stage_id = ? ");
        }
        if (statusId != null) {
            cand.append("  AND a.status_id = ? ");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ca.application_id, ap.id, ap.name, ap.email, bc.exp_years, ");
        sql.append("       st.id, st.name, st.sort_order, ");
        sql.append("       ss.id, ss.code, ss.name, pt.code ");
        sql.append("FROM ( ").append(cand).append(") ca ");
        sql.append("JOIN applicant ap ON ap.id = ca.applicant_id ");
        sql.append("JOIN stage st ON st.id = ca.stage_id ");
        sql.append("JOIN status ss ON ss.id = ca.status_id ");
        sql.append("JOIN job_posting jp2 ON jp2.id = ca.job_posting_id ");
        sql.append("JOIN position_type pt ON pt.id = jp2.position_type_id ");
        sql.append("JOIN ( ");
        sql.append("  SELECT c.applicant_id, SUM(c.years) AS exp_years ");
        sql.append("  FROM career c ");
        sql.append("  WHERE c.position_type_id = ? ");
        sql.append("    AND c.applicant_id IN (SELECT x.applicant_id FROM (").append(cand).append(") x) ");
        sql.append("  GROUP BY c.applicant_id HAVING SUM(c.years) >= ? ");
        sql.append(") bc ON bc.applicant_id = ca.applicant_id ");
        sql.append("JOIN ( ");
        sql.append("  SELECT c.applicant_id ");
        sql.append("  FROM career c ");
        sql.append("  JOIN career_skill cs ON cs.career_id = c.id ");
        sql.append("  WHERE c.position_type_id = ? ");
        sql.append("    AND cs.skill_id IN (").append(skillIn).append(") ");
        sql.append("    AND c.applicant_id IN (SELECT x.applicant_id FROM (").append(cand).append(") x) ");
        sql.append("  GROUP BY c.applicant_id ");
        sql.append("  HAVING COUNT(DISTINCT cs.skill_id) = ? ");
        sql.append(") qs ON qs.applicant_id = ca.applicant_id ");
        sql.append("WHERE EXISTS ( ");
        sql.append("  SELECT 1 FROM education e ");
        if (requireCs) {
            sql.append("  JOIN major m ON m.id = e.major_id ");
            sql.append("  WHERE e.applicant_id = ca.applicant_id ");
            sql.append("    AND e.degree_id = ? AND m.is_cs_related = TRUE ");
        } else {
            sql.append("  WHERE e.applicant_id = ca.applicant_id AND e.degree_id = ? ");
        }
        sql.append(") ");
        sql.append("ORDER BY st.sort_order, ss.id, ap.id");

        List<Object> args = new ArrayList<>();
        // ca
        appendCandidateArgs(args, companyId, positionId, filter, stageId, statusId);
        // bc
        args.add(positionId);
        appendCandidateArgs(args, companyId, positionId, filter, stageId, statusId);
        args.add(minYears);
        // qs
        args.add(positionId);
        args.addAll(skillIds);
        appendCandidateArgs(args, companyId, positionId, filter, stageId, statusId);
        args.add(skillIds.size());
        // education
        args.add(degreeId);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new QualifiedApplicantResponse(
                        rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4),
                        rs.getBigDecimal(5),
                        rs.getLong(6), rs.getString(7), rs.getInt(8),
                        rs.getLong(9), rs.getString(10), rs.getString(11),
                        rs.getString(12)
                ),
                args.toArray());
    }

    private void appendCandidateArgs(List<Object> args, Long companyId, Long positionId,
                                     QualifiedFilterRequest filter, Long stageId, Long statusId) {
        args.add(companyId);
        args.add(positionId);
        if (filter.getJobPostingId() != null) {
            args.add(filter.getJobPostingId());
        }
        if (stageId != null) {
            args.add(stageId);
        }
        if (statusId != null) {
            args.add(statusId);
        }
    }

    private double resolveMinYears(QualifiedFilterRequest filter, String positionCode) {
        if (filter.getMinYears() != null) {
            if (filter.getMinYears() < 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST.value(), "INVALID_MIN_YEARS",
                        "minYears must be >= 0");
            }
            return filter.getMinYears();
        }
        return "BACKEND".equals(positionCode) ? 5.0 : 3.0;
    }

    private Long resolveStageId(Long companyId, Integer stageSortOrder) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM stage WHERE company_id = ? AND sort_order = ?",
                    Long.class, companyId, stageSortOrder);
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND.value(), "STAGE_NOT_FOUND",
                    "stage not found for companyId=" + companyId
                            + ", stageSortOrder=" + stageSortOrder);
        }
    }

    private List<Long> resolveSkillIds(Long companyId, Long positionId, QualifiedFilterRequest filter) {
        List<String> keys = filter.normalizedSkillKeys();
        if (!keys.isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (String key : keys) {
                try {
                    Long id = jdbcTemplate.queryForObject(
                            "SELECT id FROM skill WHERE skill_key = ?", Long.class, key);
                    ids.add(id);
                } catch (EmptyResultDataAccessException ex) {
                    throw new ApiException(HttpStatus.NOT_FOUND.value(), "SKILL_NOT_FOUND",
                            "skillKeys not found: " + key);
                }
            }
            return ids;
        }

        List<Long> fromPosting = jdbcTemplate.query(
                "SELECT DISTINCT jps.skill_id FROM job_posting jp " +
                        "JOIN job_posting_skill jps ON jps.job_posting_id = jp.id " +
                        "WHERE jp.company_id = ? AND jp.position_type_id = ? AND jp.is_active = TRUE" +
                        (filter.getJobPostingId() == null ? "" : " AND jp.id = ?"),
                (rs, rowNum) -> rs.getLong(1),
                filter.getJobPostingId() == null
                        ? new Object[]{companyId, positionId}
                        : new Object[]{companyId, positionId, filter.getJobPostingId()});
        return fromPosting == null ? Collections.emptyList() : fromPosting;
    }

    private Long requireOne(String sql, String notFoundMessage, Object... args) {
        try {
            Long id = jdbcTemplate.queryForObject(sql, Long.class, args);
            if (id == null) {
                throw new ApiException(HttpStatus.NOT_FOUND.value(), "MASTER_NOT_FOUND", notFoundMessage);
            }
            return id;
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND.value(), "MASTER_NOT_FOUND", notFoundMessage);
        }
    }

    /**
     * stage / status 별 분류 조회. 둘 다 생략 시 해당 company 전체.
     */
    public List<PendingApplicantResponse> findByStageAndStatus(Long companyId,
                                                               Integer stageSortOrder,
                                                               String stageName,
                                                               String statusCode) {
        requireCompanyId(companyId);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.id, ap.id, ap.name, ap.email, ");
        sql.append("       st.id, st.name, ss.id, ss.code, ss.name, ");
        sql.append("       jp.id, jp.title ");
        sql.append("FROM application a ");
        sql.append("JOIN applicant ap ON ap.id = a.applicant_id ");
        sql.append("JOIN stage st ON st.id = a.stage_id ");
        sql.append("JOIN status ss ON ss.id = a.status_id ");
        sql.append("JOIN job_posting jp ON jp.id = a.job_posting_id ");
        sql.append("WHERE a.company_id = ? ");
        sql.append("  AND st.company_id = a.company_id ");
        sql.append("  AND jp.company_id = a.company_id ");

        List<Object> args = new ArrayList<>();
        args.add(companyId);

        if (stageSortOrder != null) {
            sql.append("  AND st.sort_order = ? ");
            args.add(stageSortOrder);
        } else if (stageName != null && !stageName.isBlank()) {
            sql.append("  AND st.name = ? ");
            args.add(stageName);
        }

        if (statusCode != null && !statusCode.isBlank()) {
            sql.append("  AND ss.code = ? ");
            args.add(statusCode.trim().toUpperCase());
        }

        sql.append("ORDER BY st.sort_order, ss.id, a.id");

        return jdbcTemplate.query(sql.toString(), pendingMapper(), args.toArray());
    }

    private org.springframework.jdbc.core.RowMapper<PendingApplicantResponse> pendingMapper() {
        return (rs, rowNum) -> new PendingApplicantResponse(
                rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4),
                rs.getLong(5), rs.getString(6),
                rs.getLong(7), rs.getString(8), rs.getString(9),
                rs.getLong(10), rs.getString(11)
        );
    }

    private void requireCompanyId(Long companyId) {
        if (companyId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "MISSING_COMPANY_ID",
                    "companyId is required");
        }
    }
}
