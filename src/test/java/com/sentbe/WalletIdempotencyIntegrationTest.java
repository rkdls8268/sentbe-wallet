package com.sentbe;

import com.sentbe.cash.application.WalletTransactionFacade;
import com.sentbe.cash.domain.CashLog;
import com.sentbe.cash.domain.Member;
import com.sentbe.cash.domain.Wallet;
import com.sentbe.cash.in.dto.CashRequest;
import com.sentbe.cash.out.CashLogRepository;
import com.sentbe.cash.out.MemberRepository;
import com.sentbe.cash.out.WalletRepository;
import com.sentbe.global.exception.GeneralException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

  @Test
  @DisplayName("멱등성 테스트 - 동일 transactionId 동시 요청 시 1건만 반영된다")
  void withdraw_sameTransactionId_concurrently_withIdempotency() throws Exception {
    int threadCount = 20;
    long withdrawAmount = 1000L;
    long initialBalance = 10_000L;
    String transactionId = "tx-same-concurrent";

    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger failCount = new AtomicInteger();
    List<Throwable> errors = new CopyOnWriteArrayList<>();

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          startLatch.await();

          CashRequest request = new CashRequest(memberId, withdrawAmount, transactionId);

          try {
            walletTransactionFacade.withdraw(walletId, request);
            successCount.incrementAndGet();
          } catch (GeneralException e) {
            failCount.incrementAndGet();
          } catch (Throwable t) {
            errors.add(t);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          errors.add(e);
        } finally {
          doneLatch.countDown();
        }
      });
    }

    startLatch.countDown();
    doneLatch.await();
    executorService.shutdown();

    // 최대 60초 동안 종료 대기. 안 끝나면 강제 종료
    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
      executorService.shutdownNow();
    }

    Wallet wallet = walletRepository.findById(walletId).orElseThrow();
    List<CashLog> logs = cashLogRepository.findAll();

    log.debug("Test Result - successCount = {}, finalBalance = {}, cashLogCount = {}",
      successCount.get(), wallet.getBalance(), logs.size());

    assertThat(errors).isEmpty();

    // 멱등성이 있으면 원래 successCount == 1 이어야 함
    assertThat(successCount.get()).isEqualTo(1);
    assertThat(logs.size()).isEqualTo(1);
    assertThat(wallet.getBalance()).isEqualTo(initialBalance - withdrawAmount);
  }
}
