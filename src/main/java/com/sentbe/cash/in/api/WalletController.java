package com.sentbe.cash.in.api;

import com.sentbe.cash.application.WalletService;
import com.sentbe.cash.application.WalletTransactionFacade;
import com.sentbe.cash.in.dto.CashRequest;
import com.sentbe.cash.in.dto.CashLogResponse;
import com.sentbe.cash.in.dto.WalletTransactionResponse;
import com.sentbe.global.response.ApiResponse;
import com.sentbe.global.status.SuccessStatus;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

  private final WalletService walletService;
  private final WalletTransactionFacade walletTransactionFacade;

  /**
   * 월렛 출금 API
   */
  @PostMapping("/{walletId}/withdrawals")
  public ResponseEntity<ApiResponse> withdraw(
    @PathVariable Long walletId,
    @RequestBody @Valid CashRequest request
  ) {
    WalletTransactionResponse response = walletTransactionFacade.withdraw(walletId, request);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }

  /**
   * 월렛 거래내역 조회 API
   */
  @GetMapping("/{walletId}/transactions")
  public ResponseEntity<ApiResponse> getWallets(@PathVariable Long walletId) {
    List<CashLogResponse> transactions = walletService.getWallets(walletId);
    return ApiResponse.onSuccess(SuccessStatus.OK, transactions);
  }

}
