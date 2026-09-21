-- =============================================================================
-- AINJOB ATS Schema (MySQL 8+)
-- 전제: tenant = company_id. 문자열 상태 금지. 회사 일치 복합 FK.
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS app_history;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS job_posting_skill;
DROP TABLE IF EXISTS job_posting;
DROP TABLE IF EXISTS career_skill;
DROP TABLE IF EXISTS career;
DROP TABLE IF EXISTS education;
DROP TABLE IF EXISTS applicant;
DROP TABLE IF EXISTS status;
DROP TABLE IF EXISTS stage;
DROP TABLE IF EXISTS position_type;
DROP TABLE IF EXISTS skill;
DROP TABLE IF EXISTS degree;
DROP TABLE IF EXISTS major;
DROP TABLE IF EXISTS company;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE company (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_company_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE major (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    is_cs_related   TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uq_major_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE degree (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    sort_order  INT NOT NULL,
    UNIQUE KEY uq_degree_name (name),
    UNIQUE KEY uq_degree_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE skill (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    skill_key   VARCHAR(50) NOT NULL,
    skill_name  VARCHAR(100) NOT NULL,
    UNIQUE KEY uq_skill_key (skill_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE position_type (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_position_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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

CREATE TABLE status (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_status_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE applicant (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    email       VARCHAR(200) NOT NULL,
    birth_date  DATE NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE education (
    id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    applicant_id  BIGINT NOT NULL,
    school_name   VARCHAR(100) NOT NULL,
    degree_id     BIGINT NOT NULL,
    major_id      BIGINT NOT NULL,
    CONSTRAINT fk_edu_applicant FOREIGN KEY (applicant_id) REFERENCES applicant(id),
    CONSTRAINT fk_edu_degree FOREIGN KEY (degree_id) REFERENCES degree(id),
    CONSTRAINT fk_edu_major FOREIGN KEY (major_id) REFERENCES major(id),
    CONSTRAINT uq_education_applicant_degree UNIQUE (applicant_id, degree_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE career (
    id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    applicant_id      BIGINT NOT NULL,
    company_name      VARCHAR(100) NOT NULL,
    years             DECIMAL(4,1) NOT NULL,
    position_type_id  BIGINT NOT NULL,
    is_current        TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_career_applicant FOREIGN KEY (applicant_id) REFERENCES applicant(id),
    CONSTRAINT fk_career_position FOREIGN KEY (position_type_id) REFERENCES position_type(id),
    CONSTRAINT chk_career_years CHECK (years > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE career_skill (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    career_id   BIGINT NOT NULL,
    skill_id    BIGINT NOT NULL,
    CONSTRAINT fk_cs_career FOREIGN KEY (career_id) REFERENCES career(id),
    CONSTRAINT fk_cs_skill FOREIGN KEY (skill_id) REFERENCES skill(id),
    CONSTRAINT uq_career_skill UNIQUE (career_id, skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE job_posting (
    id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    company_id        BIGINT NOT NULL,
    title             VARCHAR(200) NOT NULL,
    position_type_id  BIGINT NOT NULL,
    is_active         TINYINT(1) NOT NULL DEFAULT 1,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_jp_company FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT fk_jp_position FOREIGN KEY (position_type_id) REFERENCES position_type(id),
    CONSTRAINT uq_job_posting_id_company UNIQUE (id, company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE job_posting_skill (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    job_posting_id  BIGINT NOT NULL,
    skill_id        BIGINT NOT NULL,
    CONSTRAINT fk_jps_job FOREIGN KEY (job_posting_id) REFERENCES job_posting(id),
    CONSTRAINT fk_jps_skill FOREIGN KEY (skill_id) REFERENCES skill(id),
    CONSTRAINT uq_job_posting_skill UNIQUE (job_posting_id, skill_id)
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
    -- 공고·단계의 company_id 와 application.company_id 일치 강제
    CONSTRAINT fk_app_job_company
        FOREIGN KEY (job_posting_id, company_id) REFERENCES job_posting(id, company_id),
    CONSTRAINT fk_app_stage_company
        FOREIGN KEY (stage_id, company_id) REFERENCES stage(id, company_id),
    CONSTRAINT uq_application_company_job_applicant
        UNIQUE (company_id, job_posting_id, applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE app_history (
    id               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    application_id   BIGINT NOT NULL,
    from_stage_id    BIGINT NULL,
    to_stage_id      BIGINT NOT NULL,
    from_status_id   BIGINT NULL,
    to_status_id     BIGINT NOT NULL,
    reason           VARCHAR(500) NULL,
    changed_by       VARCHAR(100) NULL,
    changed_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ah_app FOREIGN KEY (application_id) REFERENCES application(id),
    CONSTRAINT fk_ah_from_stage FOREIGN KEY (from_stage_id) REFERENCES stage(id),
    CONSTRAINT fk_ah_to_stage FOREIGN KEY (to_stage_id) REFERENCES stage(id),
    CONSTRAINT fk_ah_from_status FOREIGN KEY (from_status_id) REFERENCES status(id),
    CONSTRAINT fk_ah_to_status FOREIGN KEY (to_status_id) REFERENCES status(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE app_user (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    company_id  BIGINT NOT NULL,
    email       VARCHAR(200) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT uq_app_user_company_email UNIQUE (company_id, email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE role (
    id          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_role (
    id       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT NOT NULL,
    role_id  BIGINT NOT NULL,
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES role(id),
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- company + 직군으로 공고 선조회 후 application JOIN
CREATE INDEX idx_job_posting_company_position
    ON job_posting (company_id, position_type_id, is_active);
-- company + 공고(+ 선택 stage/status)
CREATE INDEX idx_application_company_job_stage_status
    ON application (company_id, job_posting_id, stage_id, status_id);
-- stage×status 분류 (/applications, /qualified 옵션 필터)
CREATE INDEX idx_application_company_stage_status_job_applicant
    ON application (company_id, stage_id, status_id, job_posting_id, applicant_id);
CREATE INDEX idx_education_applicant_degree_major
    ON education (applicant_id, degree_id, major_id);
CREATE INDEX idx_career_applicant_position
    ON career (applicant_id, position_type_id);
CREATE INDEX idx_career_skill_skill_career
    ON career_skill (skill_id, career_id);
