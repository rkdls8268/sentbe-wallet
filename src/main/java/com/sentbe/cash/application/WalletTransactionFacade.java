package com.sentbe.cash.application;

import com.sentbe.cash.in.dto.CashRequest;
import com.sentbe.cash.in.dto.WalletTransactionCommand;
import com.sentbe.cash.in.dto.WalletTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletTransactionFacade {

  private final WalletTransactionService walletTransactionService;
  private final IdempotencySupport idempotencySupport;

  @Transactional
  public WalletTransactionResponse withdraw(Long walletId, CashRequest request) {
    WalletTransactionCommand command = WalletTransactionCommand.withdraw(
      request.memberId(),
      walletId,
      request.amount(),
      request.transactionId()
    );

    return idempotencySupport.execute(command, walletTransactionService::withdraw);
  }

  @Transactional
  public WalletTransactionResponse deposit(Long walletId, CashRequest request) {
    WalletTransactionCommand command = WalletTransactionCommand.deposit(
      request.memberId(),
      walletId,
      request.amount(),
      request.transactionId()
    );

    return idempotencySupport.execute(command, walletTransactionService::deposit);
  }

}
