package com.sentbe.cash.application;

import com.sentbe.cash.domain.Member;
import com.sentbe.cash.domain.Wallet;
import com.sentbe.cash.in.dto.MemberDto;
import com.sentbe.cash.out.MemberRepository;
import com.sentbe.cash.out.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

  private final WalletRepository walletRepository;
  private final MemberRepository memberRepository;

  public void createWallet(MemberDto memberDto) {
    Member member = memberRepository.getReferenceById(memberDto.id());
    Wallet wallet = Wallet.create(member);
    walletRepository.save(wallet);
  }

  public void deposit() {
    // 입금

  }

  public void withdraw() {
    // 출금
  }

}
