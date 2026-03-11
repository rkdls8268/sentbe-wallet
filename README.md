# wallet 동시 출금 및 잔액 무결성 보장 시스템

### 기술 스택 및 환경
* 언어: Java 21
* framework: Spring Boot 4+
* db: RDB (PostgreSQL)

### 실행 방법
1. postgresql 실행
```shell
docker compose up -d
```
2. API 서버 실행
```shell
./gradlew bootRun --args='--spring.profiles.active=local'
```

3. 초기 데이터 setup
* ApplicationRunner를 활용한 DataInit 으로 Application 실행과 동시에 Member, Wallet 초기 데이터 세팅 완료

### API
* 월렛 입금 API
  * `POST /api/v1/wallets/{walletId}/deposits`
    * request
      ```json
        {
          "transactionId": "TXN_UUID_00007",
          "memberId": "7",
          "amount": 100
        }
        ```
    * response
      ```json
      {
        "isSuccess": true,
        "code": "200",
        "message": "성공입니다.",
        "result": {
          "transactionId": "TXN_UUID_00007",
          "walletId": 1,
          "balance": 10000
          }
      }
      ```

* 월렛 출금 API
  * `POST /api/v1/wallets/{walletId}/withdrawals`
    * request
      ```json
        {
          "transactionId": "TXN_UUID_00008",
          "memberId": "7",
          "amount": 100
        }
        ```
    * response
      ```json
      {
        "isSuccess": true,
        "code": "200",
        "message": "성공입니다.",
        "result": {
          "transactionId": "TXN_UUID_00008",
          "walletId": 1,
          "balance": 9900
          }
      }
      ```
* 월렛 거래내역 조회 API
  * `GET /api/v1/wallets/{walletId}/transactions`
    * response
    ```json
      {
        "isSuccess": true,
        "code": "200",
        "message": "성공입니다.",
        "result": [
            {
                "transactionId": "TXN_UUID_00008",
                "walletId": 1,
                "memberId": 1,
                "eventType": "입금",
                "amount": 100,
                "balance": 10000,
                "createdAt": "2026-03-11T16:31:49.982905"
            },
            {
                "transactionId": "TXN_UUID_00007",
                "walletId": 1,
                "memberId": 1,
                "eventType": "출금",
                "amount": 100,
                "balance": 9900,
                "createdAt": "2026-03-11T16:29:07.009065"
            },
            {
                "transactionId": "TXN_UUID_00001",
                "walletId": 1,
                "memberId": 1,
                "eventType": "입금",
                "amount": 10000,
                "balance": 10000,
                "createdAt": "2026-03-11T16:19:52.412152"
            }
        ]
      }
    ```

### 주요 요구사항
* [멱등성 보장](./docs/idempotency.md)
* [동시성 제어](./docs/concurrency.md)

### 추가 기능
* DDD 적용
  * 강한 결합을 제거하기 위해 ddd 적용
    * 추후 대용량 트래픽이 몰릴 시 서비스 분리에 용이 -> msa로 옮겨가기 쉽도록 ddd 적용
* 공통으로 사용하는 필드는 BaseEntity에 모아두고 상속
* exception, response
  * globalExceptionHandler 추가
  * ApiResponse 구조 통일