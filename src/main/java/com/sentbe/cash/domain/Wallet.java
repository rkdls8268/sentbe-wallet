package com.sentbe.cash.domain;

import com.sentbe.shared.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;

@Entity
@Table(name = "wallet")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Wallet extends BaseEntity {
  // 월렛 잔액 관리 테이블
  // walletId, balance

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @Builder.Default
  private BigDecimal balance = BigDecimal.ZERO;

  @OneToMany(mappedBy = "wallet", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
  private List<CashLog> cashLogs = new ArrayList<>();

  public static Wallet create(Member member) {
    return Wallet.builder()
      .member(member)
      .balance(BigDecimal.ZERO)
      .build();
  }
}
