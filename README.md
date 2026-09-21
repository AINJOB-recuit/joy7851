# AINJOB ATS Assignment

멀티테넌트 HR SaaS ATS 과제 (Java 11 / Spring Boot 2.7 / **Gradle** / **MySQL 8**).

## 빠른 실행 (H2)

```bash
./gradlew bootRun
```

Windows PowerShell:

```powershell
.\gradlew.cmd bootRun
```

## Docker로 MySQL + SMTP 연결

### 1) Docker Desktop + WSL2 준비

WSL2가 안 되면 Docker 엔진이 500 에러를 냅니다.

```powershell
wsl --install --no-distribution
wsl --update
# 재부팅 후
wsl --status
```

Docker Desktop이 **Running** 될 때까지 기다린 뒤:

```bash
docker version   # Client + Server 둘 다 보여야 함
```

### 2) MySQL + Mailpit 기동

```bash
cd ~/inflean/Spring/ainjob
docker compose up -d
docker compose ps
```

| 서비스 | 주소 |
|--------|------|
| MySQL | `localhost:3306` / DB·user·pw = `ainjob` |
| Mailpit UI | http://localhost:8025 |
| Mailpit SMTP | `localhost:1025` |

스키마·시드는 컨테이너 최초 기동 시 `docs/sql/01_schema.sql`, `02_seed.sql` 자동 적용.

초기화 다시:

```bash
docker compose down -v && docker compose up -d
```

### 3) 앱을 MySQL에 연결

```bash
./gradlew bootRun --args='--spring.profiles.active=mysql'
```

PowerShell:

```powershell
.\gradlew.cmd bootRun --args="--spring.profiles.active=mysql"
```

### 4) 연결 확인

```bash
curl "http://localhost:8080/api/v1/ats/qualified?companyId=1&positionCode=BACKEND"
```

단계 전이 시 메일은 http://localhost:8025 에서 확인.

## 주요 API

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/v1/ats/qualified?companyId=1&positionCode=BACKEND` | BE 조건충족 지원자 |
| GET | `/api/v1/ats/qualified?...&stageSortOrder=3&statusCode=PASSED` | 조건충족 + stage/status 분류 |
| GET | `/api/v1/ats/qualified?companyId=1&positionCode=FRONTEND` | FE 조건충족 지원자 |
| GET | `/api/v1/ats/applications?companyId=1&stageSortOrder=2&statusCode=PENDING` | stage×status 분류 |
| GET | `/api/v1/ats/applications?companyId=1&stageSortOrder=3` | stage만 분류 |
| GET | `/api/v1/ats/applications?companyId=1&statusCode=PENDING` | status만 분류 |
| PATCH | `/api/v1/applications/{id}/transition` | Stage×Status 전이 (+ SMTP) |

## 테스트

```bash
./gradlew test
```

## 제출물

| 항목 | 경로 |
|------|------|
| MySQL SQL | `docs/sql/` |
| API 명세 | `docs/api/transition-api.md` |
