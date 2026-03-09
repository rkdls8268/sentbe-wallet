package com.sentbe.cash.application;

import com.sentbe.cash.domain.Member;
import com.sentbe.cash.domain.Wallet;
import com.sentbe.cash.in.dto.CashRequest;
import com.sentbe.cash.in.dto.MemberDto;
import com.sentbe.cash.out.MemberRepository;
import com.sentbe.cash.out.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional
  public void deposit(Long memberId, Long amount, String transactionId) {
    // 입금
    Wallet wallet = walletRepository.findByMemberId(memberId).orElseThrow();
    wallet.credit(amount, transactionId);
  }

  @Transactional
  public void withdraw(CashRequest request) {
    // 출금
    Wallet wallet = walletRepository.findByMemberId(request.memberId()).orElseThrow();
    wallet.debit(request.amount(), request.transactionId());
  }

  public Wallet getWalletByMember(Long memberId) {
    return walletRepository.findByMemberId(memberId).orElseThrow();
  }

}
