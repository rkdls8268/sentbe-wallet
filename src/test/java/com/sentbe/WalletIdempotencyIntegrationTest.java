package com.sentbe;

import com.sentbe.cash.application.WalletTransactionFacade;
import com.sentbe.cash.domain.CashLog;
import com.sentbe.cash.domain.Member;
import com.sentbe.cash.domain.Wallet;
import com.sentbe.cash.in.dto.CashRequest;
import com.sentbe.cash.out.CashLogRepository;
import com.sentbe.cash.out.MemberRepository;
import com.sentbe.cash.out.WalletRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class WalletIdempotencyIntegrationTest {
  @Autowired
  private WalletTransactionFacade walletTransactionFacade;

  @Autowired
  private WalletRepository walletRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private CashLogRepository cashLogRepository;

  private Long memberId;
  private Long walletId;

  @BeforeEach
  void setUp() {
    cashLogRepository.deleteAllInBatch();
    walletRepository.deleteAllInBatch();
    memberRepository.deleteAllInBatch();

    Member member = Member.builder()
      .email("test-user@test.com")
      .nickname("test-user")
      .password("1234")
      .build();
    memberRepository.save(member);

    Wallet wallet = Wallet.builder()
      .member(member)
      .balance(10_000L) // 1000원씩 10번까지만 성공 가능
      .build();
    walletRepository.save(wallet);

    memberId = member.getId();
    walletId = wallet.getId();
  }

  @Test
  @DisplayName("멱등성 테스트 - 멱등성 보장 순차 재호출")
  void withdraw_sameTransactionId_twice_withIdempotency() {
    // given
    String transactionId = "TXN_UUID_00001";
    long withdrawAmount = 1000L;

    CashRequest request = new CashRequest(memberId, withdrawAmount, transactionId);

    // when
    walletTransactionFacade.withdraw(walletId, request);
    walletTransactionFacade.withdraw(walletId, request);

    // then
    Wallet wallet = walletRepository.findById(walletId).orElseThrow();
    List<CashLog> logs = cashLogRepository.findAll();

    log.debug("Test Result - finalBalance = {}, cashLogCount = {}",
      wallet.getBalance(), logs.size());

    assertThat(wallet.getBalance()).isEqualTo(9000L);
    assertThat(logs).hasSize(1);
    assertThat(logs).extracting(CashLog::getTransactionId)
      .containsExactlyInAnyOrder(transactionId);
  }
}
