package com.sentbe.cash.in;

import com.sentbe.cash.application.MemberService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Profile("!prod")
@Configuration
public class MemberDataInit {
  private final MemberDataInit self;
  private final MemberService memberService;

  public MemberDataInit(@Lazy MemberDataInit self, MemberService memberService) {
    this.self = self;
    this.memberService = memberService;
  }

  @Bean
  @Order(1)
  public ApplicationRunner cashMemberDataInitApplicationRunner() {
    return args -> {
      self.makeBaseMembers();
    };
  }

  @Transactional
  public void makeBaseMembers() {
    if (memberService.count() > 0) return;

    memberService.register("user1@test.com", "user1", "1234");
    memberService.register("user2@test.com", "user2", "1234");
    memberService.register("user3@test.com", "user3", "1234");
  }
}
