package com.sentbe.cash.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CashRequest(
  @NotNull(message = "memberId는 필수입니다.")
  Long memberId,
  @Positive(message = "금액은 0보다 커야합니다.")
  Long amount,
  @NotBlank(message = "transactionId는 필수입니다.")
  String transactionId
) {}
