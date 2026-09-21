# 상태 변경(전이) API 명세

## 1. 모델: Stage × Status (문자열 상태 금지)

| 축 | 의미 | 예시 (기업1 seed) |
|----|------|-------------------|
| **Stage** | 채용 파이프라인 단계 | 1 서류접수 → 2 면접 → 3 최종합격 |
| **Status** | 단계 내 처리 상태 | `IN_PROGRESS` 진행중 / `PENDING` 보류 / `PASSED` 합격 / `REJECTED` 불합격 |

- Request·Response 모두 **ID 기반** (`targetStageId`, `targetStatusId`).
- `application` 테이블에 status 문자열 컬럼 없음. FK만 사용.
- 테넌트 격리: body의 `companyId`와 path의 `applicationId`가 같은 회사여야 함.

---

## 2. Endpoint

```
PATCH /api/v1/applications/{applicationId}/transition
Content-Type: application/json
```

### Path

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| applicationId | long | Y | 지원(Application) PK |

### Request Body

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| companyId | long | Y | 멀티테넌트 회사 ID |
| targetStageId | long | Y | 이동할 Stage ID (해당 company 소속) |
| targetStatusId | long | Y | 이동할 Status ID (공통 마스터) |
| reason | string | N | 변경 사유 → `app_history.reason` |
| changedBy | string | N | 변경자 식별자 → `app_history.changed_by` |

```json
{
  "companyId": 1,
  "targetStageId": 2,
  "targetStatusId": 1,
  "reason": "보류 → 진행중 복구",
  "changedBy": "recruiter@company1.com"
}
```

> seed 기준: applicationId=`1` 은 (면접/`stageId=2`, 보류/`statusId=2`).  
> 보류 해제 시 `targetStageId=2`, `targetStatusId=1` (`IN_PROGRESS`).

### Response 200 OK

```json
{
  "applicationId": 1,
  "companyId": 1,
  "stageId": 2,
  "stageName": "면접",
  "statusId": 1,
  "statusCode": "IN_PROGRESS",
  "statusName": "진행중"
}
```

### 부가 동작 (성공 시)

1. `application.stage_id` / `status_id` 갱신  
2. `app_history` INSERT (from/to stage·status, reason, changedBy)  
3. SMTP 알림 시도 (실패해도 전이는 커밋 — Mailpit `http://localhost:8025`)

---

## 3. HTTP Status

| Code | 코드(body) | 언제 |
|------|------------|------|
| **200** | — | 전이 성공 |
| **400** | `MISSING_COMPANY_ID` / validation | `companyId` 등 필수값 누락·형식 오류 |
| **401** | — | 미인증 (운영 JWT — 과제 데모 생략) |
| **403** | — | 타 테넌트·권한 없음 (운영) |
| **404** | `NOT_FOUND` 등 | application/stage/status 없음 또는 company 불일치 |
| **409** | `INVALID_TRANSITION` | 허용되지 않는 Stage×Status 전이 |

> **409** 선택 이유: 요청 문법 오류(422)가 아니라 **현재 리소스 상태와 충돌**하는 비즈니스 규칙 위반.

에러 body 예:

```json
{
  "code": "INVALID_TRANSITION",
  "message": "보류 상태에서는 먼저 진행중으로 복구한 뒤 합격/불합격을 처리해야 합니다."
}
```

---

## 4. 전이 규칙

### A. 동일 Stage 내 Status

| From → To | 허용 | 비고 |
|-----------|------|------|
| 진행중 → 보류 | O | 심사 보류 |
| **보류 → 진행중** | O | 동일 Stage만 |
| 진행중 → 합격 / 불합격 | O | 단계 판정 |
| 보류 → 합격 / 불합격 | X | 먼저 진행중 복구 |
| 합격 → 진행중 | O | 합격 취소(동일 Stage) |
| 불합격 → 진행중 | O | 재심사(동일 Stage만) |
| 동일 Stage+Status | X | no-op 금지 → 409 |

### B. Stage 전진 (`sort_order +1`)

| 조건 | 허용 |
|------|------|
| 현재 Status=`PASSED` → 다음 Stage(`IN_PROGRESS` 또는 `PASSED`) | O |
| 현재 Status ≠ 합격 | X |
| `sort_order` 2칸 이상 점프 | X |

### C. Stage 후퇴 (`sort_order -1`)

| 조건 | 허용 |
|------|------|
| 현재=진행중/보류 → 이전 Stage=`PASSED` | O |
| 현재=합격/불합격에서 후퇴 | X |

### D. 시나리오

| # | 전이 | 결과 |
|---|------|------|
| 1 | `(면접, 보류) → (면접, 진행중)` | ✅ 200 |
| 2 | `(면접, 합격) → (최종합격, 진행중)` | ✅ 200 |
| 3 | `(서류접수, 합격) → (최종합격, *)` | ❌ 409 |
| 4 | `(면접, 보류) → (면접, 합격)` | ❌ 409 |

---

## 5. cURL

```bash
# 보류 → 진행중 (applicationId=1, 기업1)
curl -X PATCH "http://localhost:8080/api/v1/applications/1/transition" \
  -H "Content-Type: application/json" \
  -d "{\"companyId\":1,\"targetStageId\":2,\"targetStatusId\":1,\"reason\":\"보류 해제\",\"changedBy\":\"recruiter\"}"
```

---

## 6. 검증용 요청 URL (필터 / 에러)

앱 기동 후 브라우저·Postman·curl 로 확인.

### (1) 면접 단계 + 보류 중인 지원자만

기업1 seed: `stage.sort_order=2`(면접), `statusCode=PENDING` → **김똘똘** 1건.

```
GET http://localhost:8080/api/v1/ats/applications?companyId=1&stageSortOrder=2&statusCode=PENDING
```

```bash
curl "http://localhost:8080/api/v1/ats/applications?companyId=1&stageSortOrder=2&statusCode=PENDING"
```

기대: HTTP 200, 배열 size=1, `applicantName` = `김똘똘`.

### (2) companyId 없이 요청

```
GET http://localhost:8080/api/v1/ats/qualified?positionCode=BACKEND
```

```bash
curl "http://localhost:8080/api/v1/ats/qualified?positionCode=BACKEND"
```

기대: HTTP **400**, body 예:

```json
{
  "code": "MISSING_PARAM",
  "message": "Required request parameter 'companyId' for method parameter type Long is not present"
}
```
