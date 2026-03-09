package com.sentbe.cash.in;

import com.sentbe.cash.application.WalletService;
import com.sentbe.cash.domain.Wallet;
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
    Wallet wallet1 = walletService.getWalletByMember(1L);
    if (wallet1.hasBalance()) return;
    walletService.deposit(1L, 10_000L, "TXN_UUID_00001");

    Wallet wallet2 = walletService.getWalletByMember(2L);
    if (wallet2.hasBalance()) return;
    walletService.deposit(2L, 20_000L, "TXN_UUID_00002");

    Wallet wallet3 = walletService.getWalletByMember(3L);
    if (wallet3.hasBalance()) return;
    walletService.deposit(3L, 30_000L, "TXN_UUID_00003");

  }

}
