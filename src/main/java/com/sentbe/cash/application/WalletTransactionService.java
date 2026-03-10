package com.sentbe.cash.application;

import com.sentbe.cash.domain.Wallet;
import com.sentbe.cash.in.dto.WalletTransactionCommand;
import com.sentbe.cash.in.dto.WalletTransactionResponse;
import com.sentbe.cash.out.WalletRepository;
import com.sentbe.global.exception.GeneralException;
import com.sentbe.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletTransactionService {
  private final WalletRepository walletRepository;

  public WalletTransactionResponse withdraw(WalletTransactionCommand command) {
    Wallet wallet = walletRepository.findById(command.walletId())
      .orElseThrow(() -> new GeneralException(ErrorStatus.WALLET_NOT_FOUND));

    wallet.debit(command.amount(), command.transactionId());

    return WalletTransactionResponse.of(
      command.transactionId(),
      wallet.getId(),
      wallet.getBalance()
    );
  }

  public WalletTransactionResponse deposit(WalletTransactionCommand command) {
    Wallet wallet = walletRepository.findById(command.walletId())
      .orElseThrow(() -> new GeneralException(ErrorStatus.WALLET_NOT_FOUND));

    wallet.credit(command.amount(), command.transactionId());

    return WalletTransactionResponse.of(
      command.transactionId(),
      wallet.getId(),
      wallet.getBalance()
    );
  }
}
