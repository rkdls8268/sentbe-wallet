package com.sentbe.cash.in;

import com.sentbe.cash.application.WalletService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Profile("!prod")
@Configuration
public class WalletDataInit {
  private final WalletDataInit self;
  private final WalletService walletService;

  public WalletDataInit(@Lazy WalletDataInit self, WalletService walletService) {
    this.self = self;
    this.walletService = walletService;
  }

  @Bean
  @Order(2)
  public ApplicationRunner walletDataInitApplicationRunner() {
    return args -> {
      self.makeBaseWallets();
    };
  }

  @Transactional
  public void makeBaseWallets() {
    // TODO 초기화 코드
    walletService.deposit();
  }

}
