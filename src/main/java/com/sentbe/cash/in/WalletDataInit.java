package com.sentbe.cash.in;

import com.sentbe.cash.application.WalletService;
import com.sentbe.cash.application.WalletTransactionService;
import com.sentbe.cash.domain.Wallet;
import com.sentbe.cash.domain.WalletRequestType;
import com.sentbe.cash.in.dto.WalletTransactionCommand;
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
  private final WalletTransactionService walletTransactionService;

  public WalletDataInit(
    @Lazy WalletDataInit self,
    WalletService walletService,
    WalletTransactionService walletTransactionService
  ) {
    this.self = self;
    this.walletService = walletService;
    this.walletTransactionService = walletTransactionService;
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
    WalletTransactionCommand command1 = new WalletTransactionCommand(1L, 1L, 10_000L,
      "TXN_UUID_00001", WalletRequestType.DEPOSIT);
    walletTransactionService.deposit(command1);

    Wallet wallet2 = walletService.getWalletByMember(2L);
    if (wallet2.hasBalance()) return;
    WalletTransactionCommand command2 = new WalletTransactionCommand(2L, 2L, 20_000L,
      "TXN_UUID_00002", WalletRequestType.DEPOSIT);
    walletTransactionService.deposit(command2);

    Wallet wallet3 = walletService.getWalletByMember(3L);
    if (wallet3.hasBalance()) return;
    WalletTransactionCommand command3 = new WalletTransactionCommand(3L, 3L, 30_000L,
      "TXN_UUID_00003", WalletRequestType.DEPOSIT);
    walletTransactionService.deposit(command3);
  }

}
