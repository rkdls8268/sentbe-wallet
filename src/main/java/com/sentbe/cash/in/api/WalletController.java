package com.sentbe.cash.in.api;

import com.sentbe.cash.application.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

  private final WalletService walletService;

  /**
   * 월렛 출금 API
   */
  @PostMapping("/withdraw")
  public void withdraw() {
    walletService.withdraw();
  }
}
