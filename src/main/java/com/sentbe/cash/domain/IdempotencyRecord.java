package com.sentbe.cash.domain;

import com.sentbe.cash.domain.exception.IdempotencyConflictException;
import com.sentbe.shared.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
  name = "idempotency_record",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "uk_member_transaction",
      columnNames = {"member_id", "transaction_id"}
    )
  }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecord extends BaseEntity {
  @Column(nullable = false, updatable = false)
  private Long memberId;

  @Column(nullable = false, updatable = false, length = 100)
  private String transactionId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false, length = 30)
  private WalletRequestType requestType;

  @Column(nullable = false, updatable = false)
  private Long amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private IdempotencyStatus status;

  @Column(name = "response_code", length = 100)
  private String responseCode;

  @Column(name = "response_message", length = 255)
  private String responseMessage;

  @Lob
  @Column(name = "response_body")
  private String responseBody;

  private IdempotencyRecord(
    Long memberId,
    String transactionId,
    WalletRequestType requestType,
    Long amount
  ) {
    this.memberId = memberId;
    this.transactionId = transactionId;
    this.requestType = requestType;
    this.amount = amount;
    this.status = IdempotencyStatus.PROCESSING;
  }

  public static IdempotencyRecord processing(
    Long memberId,
    String transactionId,
    WalletRequestType requestType,
    Long amount
  ) {
    return new IdempotencyRecord(memberId, transactionId, requestType, amount);
  }

  public void markSuccess(String responseCode, String responseMessage, String responseBody) {
    this.status = IdempotencyStatus.SUCCESS;
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.responseBody = responseBody;
  }

  public void markFailed(String responseCode, String responseMessage, String responseBody) {
    this.status = IdempotencyStatus.FAILED;
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.responseBody = responseBody;
  }

  public void validateSameRequest(WalletRequestType requestType, Long amount) {
    if (this.requestType != requestType || !Objects.equals(this.amount, amount)) {
      throw new IdempotencyConflictException("동일 transactionId로 다른 요청이 들어왔습니다.");
    }
  }
}
