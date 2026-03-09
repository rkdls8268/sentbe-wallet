package com.sentbe.cash.in.api;

import com.sentbe.cash.application.WalletService;
import com.sentbe.cash.in.dto.CashRequest;
import com.sentbe.cash.in.dto.WalletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public void withdraw(@RequestBody CashRequest request) {
    walletService.withdraw(request);
  }

  /**
   * 월렛 거래내역 조회 API
   */
  @GetMapping("/{id}")
  public List<WalletResponse> getWallets(@PathVariable Long id) {
    return walletService.getWallets(id);
  }

}
