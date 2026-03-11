# 출금 API 멱등성(Idempotency) 보장

## 개요

출금 API는 네트워크 재시도, 클라이언트 중복 요청 등으로 인해 **동일한 요청이 여러 번 전달될 수 있습니다.**

예를 들어 동일한 요청이 다음과 같이 여러 번 전달될 수 있습니다.
```text
transactionId: TXN-123
memberId: 1
amount: 1,000
```

이 경우 멱등성이 보장되지 않으면 다음과 같은 문제가 발생할 수 있습니다.

- 동일 출금 요청이 여러 번 처리됨
- 잔액이 중복 차감됨
- Cash Log가 여러 건 생성됨

이를 방지하기 위해 **`transactionId` 기반 멱등성 처리 구조를 구현했습니다.**

---

## 멱등성 처리 구조

### IdempotencyRecord 테이블

멱등성 처리를 위해 **IdempotencyRecord 테이블을 생성하여 요청 상태를 관리**합니다.

| 컬럼               | 설명                         |
|------------------|----------------------------|
| member_id        | 요청한 사용자                    |
| transaction_id   | 클라이언트가 전달한 고유 요청 ID        |
| request_type     | 요청 타입 (DEPOSIT / WITHDRAW) |
| amount           | 요청 금액                      |
| status           | 처리 상태                      |
| response_code    | 처리 결과 코드                   |
| response_message | 처리 결과 message              |
| response_body    | 성공 시 응답 JSON               |

### Unique Key
`(member_id, transaction_id)`
* 동일 요청은 하나만 생성 가능

---

### 멱등성 상태

`IdempotencyStatus`

| 상태 | 설명 |
|---|---|
| PROCESSING | 요청 처리 중 |
| SUCCESS | 정상 처리 완료 |
| FAILED | 처리 실패 |

---

## 멱등성 처리 흐름

### 1. 기존 요청 확인

요청이 들어오면 먼저 **동일 transactionId 요청이 존재하는지 확인**합니다.

```java
Optional<IdempotencyRecord> existing =
  idempotencyRecordRepository.findByMemberIdAndTransactionId(
    command.memberId(),
    command.transactionId()
  );
```

이미 존재하는 경우 다음과 같이 처리됩니다.

| 상태         | 처리          |
| ---------- | ----------- |
| SUCCESS    | 기존 성공 응답 반환 |
| FAILED     | 기존 실패 응답 반환 |
| PROCESSING | 현재 요청 처리 중  |

### 2. PROCESSING 상태 선점
기존 요청이 없으면 PROCESSING 상태 레코드를 생성합니다.
```java
idempotencyRecordService.createProcessing(
    command.memberId(),
    command.transactionId(),
    command.requestType(),
    command.amount()
);
```
이 레코드는 다음 제약 조건을 가집니다.
```text
(member_id, transaction_id)
UNIQUE
```
따라서 동시에 여러 요청이 들어와도 하나의 요청만 PROCESSING 상태를 생성할 수 있습니다.

### 3. 동시 요청 처리
두 개 이상의 동일 요청이 동시에 들어오면 다음 상황이 발생합니다.

* Thread A → PROCESSING insert 성공
* Thread B → UNIQUE constraint 발생

이 경우 DataIntegrityViolationException을 통해 기존 레코드를 복구합니다.
```java
catch (DataIntegrityViolationException e) {
  return recoverExistingRecord(command);
}
```

### 4. 처리 상태에 따른 분기
복구된 레코드 상태에 따라 다음과 같이 처리

| 상태         | 동작          |
| ---------- | ----------- |
| SUCCESS    | 기존 응답 복원    |
| FAILED     | 기존 실패 응답 반환 |
| PROCESSING | 다른 요청이 처리 중 |

```java
if (IdempotencyStatus.PROCESSING.equals(record.getStatus())) {
  throw new GeneralException(ErrorStatus.DUPLICATE_REQUEST_IN_PROGRESS);
}
```

## 실제 트랜잭션 실행
멱등성 레코드가 PROCESSING 상태일 경우 실제 비즈니스 로직을 실행합니다.
```java
response = action.apply(command);
```

여기서 `action`은 실제 출금 로직입니다.

## 처리 결과 저장
### 성공 처리
```java
idempotencyRecordService.markSuccess(
  recordId,
  "OK",
  "정상 처리되었습니다.",
  jsonConverter.toJson(response)
);
```
저장되는 정보
* 상태 → SUCCESS
* 응답 코드
* 응답 Body
이후 동일 요청이 들어오면 응답을 그대로 복원하여 반환합니다.

### 실패 처리

```java
saveFailure(record.getId(), "INSUFFICIENT_BALANCE", e.getMessage(), null);
```
이후 동일 요청이 들어오면 기존 실패 응답을 반환합니다.

## 응답 복원
성공한 요청의 경우 응답을 JSON으로 저장 후 재사용
```java
return jsonConverter.fromJson(
  record.getResponseBody(),
  WalletTransactionResponse.class
);
```
따라서 동일 요청이 다시 들어와도 비즈니스 로직을 다시 실행하지 않고 기존 결과를 반환합니다.

## 멱등성 처리 결과
멱등성 처리를 통해 다음 문제 해결 가능

| 문제          | 해결 방법               |
| ----------- | ------------------- |
| 네트워크 재시도    | 기존 응답 반환            |
| 클라이언트 중복 요청 | 중복 실행 방지            |
| 동일 요청 중복 출금 | transactionId 기반 차단 |

## 정리

- 동일 요청은 한 번만 처리
- 중복 요청은 기존 결과 반환

## 참고 코드
* [IdempotencySupport.java](../src/main/java/com/sentbe/cash/application/IdempotencySupport.java)
* [IdempotencyRecordService.java](../src/main/java/com/sentbe/cash/application/IdempotencyRecordService.java)
* [IdempotencyRecordRepository.java](../src/main/java/com/sentbe/cash/out/IdempotencyRecordRepository.java)
* [IdempotencyRecord.java](../src/main/java/com/sentbe/cash/domain/IdempotencyRecord.java)