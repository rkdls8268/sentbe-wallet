package com.sentbe.cash.domain;

import com.sentbe.global.exception.GeneralException;
import com.sentbe.global.status.ErrorStatus;
import com.sentbe.shared.jpa.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallet")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseEntity {
  // 월렛 잔액 관리 테이블
  // walletId, balance

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @Builder.Default
  private Long balance = 0L;

  @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CashLog> cashLogs = new ArrayList<>();

  public static Wallet create(Member member) {
    return Wallet.builder()
      .member(member)
      .balance(0L)
      .build();
  }

  public void credit(Long amount, String transactionId) {
    this.balance += amount;
    addCashLog(amount, EventType.입금, transactionId, member);

  }

  public void debit(Long amount, String transactionId) {
    if (balance < amount) {
      throw new GeneralException(ErrorStatus.BALANCE_NOT_ENOUGH);
    }
    this.balance -= amount;
    addCashLog(amount, EventType.출금, transactionId, member);
  }

  public boolean hasBalance() {
    return this.balance > 0;
  }

  private CashLog addCashLog(long amount, EventType eventType, String transactionId, Member member) {
    CashLog cashLog = new CashLog(
      transactionId,
      member,
      this,
      eventType,
      amount,
      balance
    );

    cashLogs.add(cashLog);
    return cashLog;
  }
}
