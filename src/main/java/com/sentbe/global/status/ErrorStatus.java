package com.sentbe.global.status;

import java.util.Optional;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus {
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 에러, 관리자에게 문의 바랍니다."),
  // 입력값 검증 관련 에러
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "C002", "입력값이 올바르지 않습니다."),

  WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "W001", "존재하지 않는 wallet 입니다."),
  INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "W002", "잔액이 부족합니다."),
  TRANSACTION_CONFLICT(HttpStatus.CONFLICT, "W003", "동일 transactionId로 다른 요청이 들어왔습니다."),
  IDEMPOTENCY_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "W004", "존재하지 않는 idempotency record 입니다."),
  DUPLICATE_REQUEST_IN_PROGRESS(HttpStatus.BAD_REQUEST, "W005", "동일한 요청이 처리중입니다."),
  INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "W006", "입출금액은 0보다 커야 합니다."),
  IDEMPOTENCY_SAVE_FAILED(HttpStatus.BAD_REQUEST, "W007", "요청 처리 결과를 최종 확인하지 못했습니다. 동일 transactionId로 다시 조회하거나 재시도해주세요.")
  ;

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  public String getMessage(String message) {
    return Optional.ofNullable(message)
      .filter(Predicate.not(String::isBlank))
      .orElse(this.getMessage());
  }
}