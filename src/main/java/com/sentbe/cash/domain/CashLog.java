package com.sentbe.cash.domain;

import com.sentbe.shared.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cash_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CashLog extends BaseEntity {
  // 입출금 내역 테이블
  // 일련번호, walletId, 출금액, 잔액, 출금일시

  private String transactionId; // 일련번호

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;
  @ManyToOne(fetch = FetchType.LAZY)
  private Wallet wallet;

  @Enumerated(EnumType.STRING)
  private EventType eventType;
  private Long amount;
  private Long balance; // 잔액

  public CashLog(String transactionId, Member member, Wallet wallet, EventType eventType, Long amount, Long balance) {
    this.transactionId = transactionId;
    this.member = member;
    this.wallet = wallet;
    this.eventType = eventType;
    this.amount = amount;
    this.balance = balance;
  }
}
