INSERT INTO company (id, name) VALUES (1, '기업1'), (2, '기업2');

INSERT INTO degree (id, name, sort_order) VALUES
 (1, '학사', 1), (2, '석사', 2), (3, '박사', 3);

INSERT INTO major (id, name, is_cs_related) VALUES
 (1, '컴퓨터공학', TRUE),
 (2, '소프트웨어공학', TRUE),
 (3, '토목공학', FALSE),
 (4, '인공지능', FALSE),
 (5, '의상디자인', FALSE);

INSERT INTO position_type (id, code, name) VALUES
 (1, 'BACKEND', '백엔드개발'),
 (2, 'FRONTEND', '프런트개발');

INSERT INTO status (id, code, name) VALUES
 (1, 'IN_PROGRESS', '진행중'),
 (2, 'PENDING', '보류'),
 (3, 'PASSED', '합격'),
 (4, 'REJECTED', '불합격');

INSERT INTO stage (id, company_id, name, sort_order) VALUES
 (1, 1, '서류접수', 1),
 (2, 1, '면접', 2),
 (3, 1, '최종합격', 3),
 (4, 2, '서류접수', 1),
 (5, 2, '면접', 2),
 (6, 2, '최종합격', 3);

INSERT INTO skill (id, skill_key, skill_name) VALUES
 (1,  'ASP', 'ASP'),
 (2,  'AWS', 'AWS'),
 (3,  'CSS', 'CSS'),
 (4,  'DBA', 'DBA'),
 (5,  'DEEPLEARNING', 'Deep Learning'),
 (6,  'HTML', 'HTML'),
 (7,  'JAVA', 'Java'),
 (8,  'JAVASCRIPT', 'JavaScript'),
 (9,  'JPA', 'JPA'),
 (10, 'KOTLIN', 'Kotlin'),
 (11, 'NEXTJS', 'Next.js'),
 (12, 'PHP', 'PHP'),
 (13, 'PYTHON', 'Python'),
 (14, 'PYTORCH', 'PyTorch'),
 (15, 'REACT', 'React'),
 (16, 'SPRINGBOOT', 'Spring Boot'),
 (17, 'STRUTS', 'Struts'),
 (18, 'SYSTEMARCHITECT', 'System Architect'),
 (19, 'TENSORFLOW', 'TensorFlow'),
 (20, 'TYPESCRIPT', 'TypeScript'),
 (21, 'WEBPACK', 'Webpack');

INSERT INTO role (id, code, name) VALUES
 (1, 'RECRUITER', '채용담당자'),
 (2, 'ADMIN', '관리자');

INSERT INTO applicant (id, name, email, birth_date) VALUES
 (1,  '김똘똘', 'recruit@ainjob.com', DATE '1995-01-01'),
 (2,  '이서윤', '2win@naver.com',     DATE '1995-01-01'),
 (3,  '한예진', '3win@naver.com',     DATE '1995-01-01'),
 (4,  '최준호', '9win@naver.com',     DATE '1995-01-01'),
 (5,  '박지예', '10win@naver.com',    DATE '1995-01-01'),
 (6,  '강소희', '11win@naver.com',    DATE '1995-01-01'),
 (7,  '문지후', '13win@naver.com',    DATE '1995-01-01'),
 (8,  '김철수', '14win@naver.com',    DATE '1995-01-01'),
 (9,  '류태현', '15win@naver.com',    DATE '1995-01-01'),
 (10, '전상혁', '16win@naver.com',    DATE '1995-01-01'),
 (11, '안서연', '17win@naver.com',    DATE '1995-01-01'),
 (12, '권휘',   '18win@naver.com',    DATE '1995-01-01'),
 (13, '황도윤', 'win@naver.com',      DATE '1995-01-01'),
 (14, '권유진', 'win2@naver.com',     DATE '1995-01-01');

INSERT INTO education (applicant_id, school_name, degree_id, major_id) VALUES
 (1, '연세대학교', 1, 1), (1, '연세대학교', 2, 1),
 (2, '서울대학교', 1, 2), (2, '서울대학교', 2, 2),
 (3, '고려대학교', 1, 1),
 (4, '방송통신대학교', 1, 3), (4, '방송통신대학교', 2, 2),
 (5, '경북대학교', 1, 2), (5, '경북대학교', 2, 2),
 (6, '이화여자대학교', 1, 1),
 (7, '상명대학교', 1, 1), (7, '상명대학교', 2, 1), (7, '상명대학교', 3, 1),
 (8, '연세대학교', 1, 1), (8, '연세대학교', 2, 4),
 (9, '서울대학교', 1, 1), (9, '서울대학교', 2, 1),
 (10, '고려대학교', 1, 1), (10, '고려대학교', 2, 1), (10, '고려대학교', 3, 1),
 (11, '중앙대학교', 1, 1), (11, '중앙대학교', 2, 2),
 (12, '한영대학교', 1, 5), (12, '한영대학교', 2, 4),
 (13, '한양대학교', 1, 1),
 (14, '가천대학교', 1, 2);

INSERT INTO career (id, applicant_id, company_name, years, position_type_id, is_current) VALUES
 (1, 1, '네이버', 3, 1, TRUE),
 (2, 1, '카카오', 2, 2, FALSE),
 (3, 2, '네이버', 5, 1, TRUE),
 (4, 2, '다음', 5, 1, FALSE),
 (5, 3, '카카오', 3, 1, TRUE),
 (6, 3, '한화', 7, 1, FALSE),
 (7, 4, 'LG CNS', 10, 1, TRUE),
 (8, 4, '다음', 10, 1, FALSE),
 (9, 5, '네이버', 2, 2, TRUE),
 (10, 5, '당근', 1, 2, FALSE),
 (11, 6, 'TOSS', 5, 2, TRUE),
 (12, 7, '마이크로소프트', 5, 1, TRUE),
 (13, 7, '삼성전자', 4, 2, FALSE),
 (14, 8, 'Facebook', 5, 1, TRUE),
 (15, 9, '현대', 6, 1, TRUE),
 (16, 9, '삼성전자', 6, 1, FALSE),
 (17, 10, '현대', 7, 1, TRUE),
 (18, 10, 'google', 3, 1, FALSE),
 (19, 11, 'Facebook', 3, 2, TRUE),
 (20, 12, '현대', 2, 2, TRUE),
 (21, 13, '신세계', 3, 2, TRUE),
 (22, 14, '현대 자동차', 2, 2, TRUE),
 (23, 14, '삼성전자', 1, 2, FALSE);

INSERT INTO career_skill (career_id, skill_id) VALUES
 (1,2),(1,7),(1,16),(1,4),(1,13),
 (2,6),(2,20),
 (3,2),(3,7),(3,16),(3,12),(3,9),(3,18),
 (4,2),(4,7),(4,16),
 (5,2),(5,7),(5,16),(5,1),
 (6,2),
 (7,2),(7,7),(7,16),(7,1),
 (8,1),(8,10),(8,17),
 (9,15),(9,11),(9,20),(9,6),(9,3),
 (10,15),(10,11),(10,20),(10,6),(10,3),
 (11,15),(11,11),(11,20),(11,21),
 (12,2),(12,7),(12,16),(12,9),(12,5),
 (13,15),(13,11),(13,20),(13,6),(13,3),
 (14,2),(14,7),(14,16),(14,9),(14,5),
 (15,7),(15,16),(15,2),
 (16,2),(16,7),(16,16),
 (17,2),(17,13),(17,5),
 (18,2),(18,5),
 (19,15),(19,11),(19,20),(19,21),
 (20,15),(20,11),(20,20),(20,21),
 (21,15),(21,11),(21,20),(21,21),
 (22,15),(22,11),(22,20),
 (23,6),(23,20);

INSERT INTO job_posting (id, company_id, title, position_type_id, is_active) VALUES
 (1, 1, '백엔드 개발자', 1, TRUE),
 (2, 1, '프런트엔드 개발자', 2, TRUE),
 (3, 2, '백엔드 개발자', 1, TRUE),
 (4, 2, '프런트엔드 개발자', 2, TRUE);

INSERT INTO job_posting_skill (job_posting_id, skill_id) VALUES
 (1, 7), (1, 16), (1, 2),
 (2, 15), (2, 11), (2, 20),
 (3, 7), (3, 16), (3, 2),
 (4, 15), (4, 11), (4, 20);

INSERT INTO application (id, company_id, job_posting_id, applicant_id, stage_id, status_id) VALUES
 (1, 1, 1, 1, 2, 2),
 (2, 1, 1, 2, 3, 3),
 (3, 1, 1, 3, 3, 3),
 (4, 1, 1, 4, 3, 3),
 (5, 1, 1, 7, 3, 3),
 (6, 1, 2, 5, 3, 3),
 (7, 1, 2, 6, 3, 3),
 (8, 2, 3, 8, 6, 3),
 (9, 2, 3, 9, 6, 3),
 (10, 2, 4, 11, 6, 3),
 (11, 2, 4, 13, 6, 3);

INSERT INTO app_history (application_id, from_stage_id, to_stage_id, from_status_id, to_status_id, reason, changed_by) VALUES
 (2, 1, 2, 3, 3, '서류 합격', 'recruiter'),
 (2, 2, 3, 3, 3, '면접 합격 → 최종', 'recruiter'),
 (1, 1, 2, 3, 2, '면접 보류', 'recruiter');
