package com.sentbe.cash.domain;

import com.sentbe.shared.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;

@Entity
@Table(name = "wallet")
@Getter
public class Wallet extends BaseEntity {
  // 월렛 잔액 관리 테이블
  // walletId, balance

  private BigDecimal balance;

  @OneToMany(mappedBy = "wallet", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
  private List<CashLog> cashLogs = new ArrayList<>();
}
