package com.sentbe.cash.in.dto;

public record WalletTransactionResponse(
  String transactionId,
  Long walletId,
  Long balance
) {
  public static WalletTransactionResponse of(
    String transactionId,
    Long walletId,
    Long balance
  ) {
    return new WalletTransactionResponse(transactionId, walletId, balance);
  }
}
