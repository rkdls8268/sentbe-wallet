# 얼렛 동시 출금 및 잔액 무결성 보장 시스템

* DDD 적용
  * 강한 결합을 제거하기 위해 ddd 적용
    * 추후 대용량 트래픽이 몰릴 시 서비스 분리에 용이 -> msa로 옮겨가기 쉽도록 ddd 적용
* 테스트용 초기 데이터 삽입을 위해 dataInit 활용
  * member, wallet 초기 생성 후 deposit 초기 데이터 생성
* 공통으로 사용하는 필드는 BaseEntity에 모아두고 상속
* exception, response
  * globalExceptionHandler 추가
  * ApiResponse 구조 통일
* 멱등성 보장
  * 같은 요청이 여러번 들어오는 케이스
* 동시성 제어
  * 서로 다른 요청이 동시에 들어오는 케이스
  * 비관적 락 적용