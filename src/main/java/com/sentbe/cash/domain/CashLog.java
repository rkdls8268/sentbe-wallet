package com.sentbe.cash.domain;

import com.sentbe.cash.EventType;
import com.sentbe.cash.Wallet;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Table(name = "cash_log")
@Getter
public class CashLog {
  // 입출금 내역 테이블
  // 일련번호, walletId, 출금액, 잔액, 출금일시

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String transactionId; // 일련번호

  @ManyToOne(fetch = FetchType.LAZY)
  private Wallet wallet;

  @Enumerated(EnumType.STRING)
  private EventType eventType;
  private BigDecimal amount;
  private BigDecimal balance; // 잔액

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
