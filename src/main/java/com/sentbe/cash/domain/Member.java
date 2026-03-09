package com.sentbe.cash.domain;

import com.sentbe.shared.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseEntity {

  private String email;
  private String nickname;
  private String password;

  public static Member create(String email, String nickname, String password) {
    return Member.builder()
      .email(email)
      .nickname(nickname)
      .password(password)
      .build();
  }

}
