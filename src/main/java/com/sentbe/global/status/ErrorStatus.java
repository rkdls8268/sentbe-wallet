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
  BALANCE_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "W002", "잔액이 부족합니다."),
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