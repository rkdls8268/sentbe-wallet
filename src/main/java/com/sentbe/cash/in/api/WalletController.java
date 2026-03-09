package com.sentbe.cash.in.api;

import com.sentbe.cash.application.WalletService;
import com.sentbe.cash.in.dto.CashRequest;
import com.sentbe.cash.in.dto.WalletResponse;
import com.sentbe.global.response.ApiResponse;
import com.sentbe.global.status.SuccessStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  @PostMapping("/{walletId}/withdrawals")
  public ResponseEntity<ApiResponse> withdraw(
    @PathVariable Long walletId,
    @RequestBody CashRequest request
  ) {
    walletService.withdraw(walletId, request);
    return ApiResponse.onSuccess(SuccessStatus.NO_CONTENT);
  }

  /**
   * 월렛 거래내역 조회 API
   */
  @GetMapping("/{walletId}/transactions")
  public ResponseEntity<ApiResponse> getWallets(@PathVariable Long walletId) {
    List<WalletResponse> transactions = walletService.getWallets(walletId);
    return ApiResponse.onSuccess(SuccessStatus.OK, transactions);
  }

}
