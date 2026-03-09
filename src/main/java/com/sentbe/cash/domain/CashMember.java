package com.sentbe.cash.domain;

import com.sentbe.shared.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "cash_member")
@Getter
public class CashMember extends BaseEntity {

  private String username;
  private String password;

}
