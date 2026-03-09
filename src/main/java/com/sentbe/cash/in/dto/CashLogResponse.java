package com.sentbe.cash.in.dto;

import com.sentbe.cash.domain.EventType;
import java.time.LocalDateTime;

public record CashLogResponse(
  String transactionId,
  Long walletId,
  Long memberId,
  EventType eventType,
  Long amount,
  Long balance,
  LocalDateTime createdAt
) {

}
