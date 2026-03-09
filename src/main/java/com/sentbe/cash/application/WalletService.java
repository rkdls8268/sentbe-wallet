package com.sentbe.cash.application;

import com.sentbe.cash.out.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

  private final WalletRepository walletRepository;

  public void deposit() {
    // 입금

  }

  public void withdraw() {
    // 출금
  }

}
