package com.sentbe.cash.in.dto;

import com.sentbe.cash.domain.WalletRequestType;

public record WalletTransactionCommand(
  Long memberId,
  Long walletId,
  Long amount,
  String transactionId,
  WalletRequestType requestType
) {
  public static WalletTransactionCommand withdraw(
    Long memberId, Long walletId, Long amount, String transactionId
  ) {
    return new WalletTransactionCommand(
      memberId, walletId, amount, transactionId, WalletRequestType.WITHDRAW
    );
  }

  public static WalletTransactionCommand deposit(
    Long memberId, Long walletId, Long amount, String transactionId
  ) {
    return new WalletTransactionCommand(
      memberId, walletId, amount, transactionId, WalletRequestType.DEPOSIT
    );
  }
}