-- =============================================================================
-- 목적: 기업1 BE 공고 지원자 중 학력·경력·스킬 조건을 만족하는 후보 추출
-- 전제/가정:
--   - company.name UNIQUE / jp.company_id = a.company_id
--   - stage·status 는 SELECT 로 분류용 노출 (필수 필터 아님)
--   - 단계/상태별 추가 필터 예시는 파일 하단 주석
-- 인덱스 전제:
--   idx_job_posting_company_position (company + 직군)
--   idx_application_company_job_stage_status (company + 공고 ± stage/status)
--   idx_application_company_stage_status_job_applicant (분류)
--   idx_career_applicant_position / idx_career_skill_skill_career
-- =============================================================================

WITH params AS (
    SELECT
        (SELECT id FROM company WHERE name = '기업1') AS company_id,
        (SELECT id FROM degree WHERE name = '학사') AS bachelor_degree_id,
        (SELECT id FROM position_type WHERE code = 'BACKEND') AS backend_position_id,
        (SELECT id FROM skill WHERE skill_key = 'JAVA') AS java_skill_id,
        (SELECT id FROM skill WHERE skill_key = 'SPRINGBOOT') AS springboot_skill_id,
        (SELECT id FROM skill WHERE skill_key = 'AWS') AS aws_skill_id
),
required_skills AS (
    SELECT java_skill_id AS skill_id FROM params
    UNION ALL
    SELECT springboot_skill_id FROM params
    UNION ALL
    SELECT aws_skill_id FROM params
),
-- 1) 기업1 + BE 공고 지원
candidate_applications AS (
    SELECT
        a.id AS application_id,
        a.applicant_id,
        a.stage_id,
        a.status_id,
        a.job_posting_id
    FROM application a
    JOIN job_posting jp ON jp.id = a.job_posting_id
    CROSS JOIN params p
    WHERE a.company_id = p.company_id
      AND jp.company_id = a.company_id
      AND jp.position_type_id = p.backend_position_id
),
candidate_applicants AS (
    SELECT DISTINCT applicant_id
    FROM candidate_applications
),
backend_career AS (
    SELECT c.applicant_id, SUM(c.years) AS backend_years
    FROM candidate_applicants ca
    JOIN career c ON c.applicant_id = ca.applicant_id
    CROSS JOIN params p
    WHERE c.position_type_id = p.backend_position_id
    GROUP BY c.applicant_id
    HAVING SUM(c.years) >= 5
),
qualified_skills AS (
    SELECT c.applicant_id
    FROM candidate_applicants ca
    JOIN career c ON c.applicant_id = ca.applicant_id
    JOIN career_skill cs ON cs.career_id = c.id
    JOIN required_skills rs ON rs.skill_id = cs.skill_id
    CROSS JOIN params p
    WHERE c.position_type_id = p.backend_position_id
    GROUP BY c.applicant_id
    HAVING COUNT(DISTINCT cs.skill_id) = (SELECT COUNT(*) FROM required_skills)
)
SELECT
    ca.application_id,
    ap.id AS applicant_id,
    ap.name AS applicant_name,
    ap.email,
    bc.backend_years,
    st.id AS stage_id,
    st.name AS stage_name,
    st.sort_order AS stage_sort_order,
    ss.id AS status_id,
    ss.code AS status_code,
    ss.name AS status_name
FROM candidate_applications ca
JOIN applicant ap ON ap.id = ca.applicant_id
JOIN backend_career bc ON bc.applicant_id = ca.applicant_id
JOIN qualified_skills qs ON qs.applicant_id = ca.applicant_id
JOIN stage st ON st.id = ca.stage_id
JOIN status ss ON ss.id = ca.status_id
CROSS JOIN params p
WHERE EXISTS (
    SELECT 1
    FROM education e
    JOIN major m ON m.id = e.major_id
    WHERE e.applicant_id = ca.applicant_id
      AND e.degree_id = p.bachelor_degree_id
      AND m.is_cs_related = TRUE
)
ORDER BY st.sort_order, ss.id, ap.id;

-- -----------------------------------------------------------------------------
-- (참고) stage/status 별 분류 예시
-- 면접 + 보류만:
--   AND st.sort_order = 2 AND ss.code = 'PENDING'
-- 최종합격 + 합격만:
--   AND st.sort_order = 3 AND ss.code = 'PASSED'
-- -----------------------------------------------------------------------------
