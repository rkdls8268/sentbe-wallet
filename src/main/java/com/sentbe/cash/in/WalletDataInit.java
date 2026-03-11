package com.sentbe.cash.in;

import com.sentbe.cash.application.WalletService;
import com.sentbe.cash.application.WalletTransactionFacade;
import com.sentbe.cash.domain.Wallet;
import com.sentbe.cash.in.dto.CashRequest;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Profile("!prod & !test")
@Configuration
public class WalletDataInit {
  private final WalletDataInit self;
  private final WalletService walletService;
  private final WalletTransactionFacade walletTransactionFacade;

  public WalletDataInit(
    @Lazy WalletDataInit self,
    WalletService walletService,
    WalletTransactionFacade walletTransactionFacade
  ) {
    this.self = self;
    this.walletService = walletService;
    this.walletTransactionFacade = walletTransactionFacade;
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
    CashRequest request1 = new CashRequest(1L, 10_000L, "TXN_UUID_00001");
    walletTransactionFacade.deposit(1L, request1);

    Wallet wallet2 = walletService.getWalletByMember(2L);
    if (wallet2.hasBalance()) return;
    CashRequest request2 = new CashRequest(2L, 20_000L, "TXN_UUID_00002");
    walletTransactionFacade.deposit(2L, request2);

    Wallet wallet3 = walletService.getWalletByMember(3L);
    if (wallet3.hasBalance()) return;
    CashRequest request3 = new CashRequest(3L, 30_000L, "TXN_UUID_00003");
    walletTransactionFacade.deposit(3L, request3);
  }
}
