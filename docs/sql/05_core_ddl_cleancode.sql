-- =============================================================================
-- 목적: Application / Stage / CareerSkill 핵심 DDL (MySQL, 제약조건 포함)
-- 전제/가정: company(name UK) / job_posting / applicant / skill / status 선행
-- 인덱스 전제: idx_job_posting_company_position (직군 선조회)
--              idx_application_company_job_stage_status (company+공고±stage/status)
--              idx_application_company_stage_status_job_applicant (분류)
-- =============================================================================

CREATE TABLE stage (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    company_id  BIGINT NOT NULL,
    name        VARCHAR(50) NOT NULL,
    sort_order  INT NOT NULL,
    CONSTRAINT fk_stage_company FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT uq_stage_company_order UNIQUE (company_id, sort_order),
    CONSTRAINT uq_stage_company_name  UNIQUE (company_id, name),
    CONSTRAINT uq_stage_id_company UNIQUE (id, company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE application (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    company_id      BIGINT NOT NULL,
    job_posting_id  BIGINT NOT NULL,
    applicant_id    BIGINT NOT NULL,
    stage_id        BIGINT NOT NULL,
    status_id       BIGINT NOT NULL,
    applied_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_company FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT fk_app_applicant FOREIGN KEY (applicant_id) REFERENCES applicant(id),
    CONSTRAINT fk_app_status FOREIGN KEY (status_id) REFERENCES status(id),
    CONSTRAINT fk_app_job_company
        FOREIGN KEY (job_posting_id, company_id) REFERENCES job_posting(id, company_id),
    CONSTRAINT fk_app_stage_company
        FOREIGN KEY (stage_id, company_id) REFERENCES stage(id, company_id),
    CONSTRAINT uq_application_company_job_applicant
        UNIQUE (company_id, job_posting_id, applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE career_skill (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    career_id   BIGINT NOT NULL,
    skill_id    BIGINT NOT NULL,
    CONSTRAINT fk_cs_career FOREIGN KEY (career_id) REFERENCES career(id),
    CONSTRAINT fk_cs_skill FOREIGN KEY (skill_id) REFERENCES skill(id),
    CONSTRAINT uq_career_skill UNIQUE (career_id, skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_job_posting_company_position
    ON job_posting (company_id, position_type_id, is_active);

CREATE INDEX idx_application_company_job_stage_status
    ON application (company_id, job_posting_id, stage_id, status_id);

CREATE INDEX idx_application_company_stage_status_job_applicant
    ON application (company_id, stage_id, status_id, job_posting_id, applicant_id);

CREATE INDEX idx_career_skill_skill_career
    ON career_skill (skill_id, career_id);

CREATE INDEX idx_stage_company_order
    ON stage (company_id, sort_order);
