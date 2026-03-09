package com.sentbe.cash.domain;

import com.sentbe.shared.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;

@Entity
@Table(name = "cash_log")
@Getter
public class CashLog extends BaseEntity {
  // 입출금 내역 테이블
  // 일련번호, walletId, 출금액, 잔액, 출금일시

  private String transactionId; // 일련번호

  @ManyToOne(fetch = FetchType.LAZY)
  private Wallet wallet;

  @Enumerated(EnumType.STRING)
  private EventType eventType;
  private BigDecimal amount;
  private BigDecimal balance; // 잔액
}
