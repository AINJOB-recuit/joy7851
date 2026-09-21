# 인덱스 + 필수스킬 AND 구현 메모

```
-- 목적: 필수 스킬 AND
-- 전제/가정:
--   1) job_posting(company, position) 로 공고 후보
--   2) application(company, job) 로 지원 후보 (± stage/status)
--   3) career_skill HAVING COUNT(DISTINCT) = 필수개수
-- 인덱스 전제:
--   idx_job_posting_company_position
--     (company_id, position_type_id, is_active)
--   idx_application_company_job_stage_status
--     (company_id, job_posting_id, stage_id, status_id)
--   idx_application_company_stage_status_job_applicant
--     (company_id, stage_id, status_id, job_posting_id, applicant_id)
--   idx_career_skill_skill_career (skill_id, career_id)
```

## leftmost prefix ↔ 옵션 필터

| 조회 패턴 | 사용 인덱스 | 비고 |
|-----------|-------------|------|
| company + 직군 (stage/status 없음) | `idx_job_posting_company_position` → application FK/UK | `job_posting_id` 미지정 시 application 쪽은 company(+job)만 |
| company + jobPostingId ± stage ± status | `idx_application_company_job_stage_status` | 옵션 컬럼이 뒤에 있어 생략 가능 |
| company + stage ± status (/applications, /qualified 분류) | `idx_application_company_stage_status_job_applicant` | stage 없이 status만이면 company 선두만 사용 |
| 학력 / 경력 / 스킬 AND | education·career·career_skill 인덱스 | 후보 축소 후 집계 |

## 필수 인덱스

1. `idx_job_posting_company_position` — 기본 (직군 선필터)
2. `idx_application_company_job_stage_status` — company+공고, stage/status는 trailing optional
3. `idx_application_company_stage_status_job_applicant` — stage×status 분류
4. `idx_education_applicant_degree_major`
5. `idx_career_applicant_position`
6. `idx_career_skill_skill_career`
