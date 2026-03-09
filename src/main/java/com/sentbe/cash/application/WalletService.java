package com.sentbe.cash.application;

import com.sentbe.cash.domain.CashLog;
import com.sentbe.cash.domain.Member;
import com.sentbe.cash.domain.Wallet;
import com.sentbe.cash.in.dto.CashRequest;
import com.sentbe.cash.in.dto.MemberDto;
import com.sentbe.cash.in.dto.WalletResponse;
import com.sentbe.cash.out.CashLogRepository;
import com.sentbe.cash.out.MemberRepository;
import com.sentbe.cash.out.WalletRepository;
import com.sentbe.global.exception.GeneralException;
import com.sentbe.global.status.ErrorStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

  private final WalletRepository walletRepository;
  private final MemberRepository memberRepository;
  private final CashLogRepository cashLogRepository;

  public void createWallet(MemberDto memberDto) {
    Member member = memberRepository.getReferenceById(memberDto.id());
    Wallet wallet = Wallet.create(member);
    walletRepository.save(wallet);
  }

  @Transactional
  public void deposit(Long memberId, Long amount, String transactionId) {
    // 입금
    Wallet wallet = walletRepository.findByMemberId(memberId)
      .orElseThrow(() -> new GeneralException(ErrorStatus.WALLET_NOT_FOUND));
    wallet.credit(amount, transactionId);
  }

  @Transactional
  public void withdraw(Long walletId, CashRequest request) {
    // 출금
    Wallet wallet = walletRepository.findById(walletId)
      .orElseThrow(() -> new GeneralException(ErrorStatus.WALLET_NOT_FOUND));
    wallet.debit(request.amount(), request.transactionId());
  }

  public Wallet getWalletByMember(Long memberId) {
    return walletRepository.findByMemberId(memberId)
      .orElseThrow(() -> new GeneralException(ErrorStatus.WALLET_NOT_FOUND));
  }

  public List<WalletResponse> getWallets(Long walletId) {
    boolean exists = walletRepository.existsById(walletId);
    if (!exists) {
      throw new GeneralException(ErrorStatus.WALLET_NOT_FOUND);
    }

    List<CashLog> cashLogs = cashLogRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    return cashLogs.stream().map(CashLog::toWalletResponse).toList();
  }

}
