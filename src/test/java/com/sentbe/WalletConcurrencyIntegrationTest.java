package com.sentbe;

import com.sentbe.cash.application.WalletTransactionService;
import com.sentbe.cash.domain.Member;
import com.sentbe.cash.domain.Wallet;
import com.sentbe.cash.domain.WalletRequestType;
import com.sentbe.cash.in.dto.WalletTransactionCommand;
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
public class WalletConcurrencyIntegrationTest {

  @Autowired
  private WalletTransactionService walletTransactionService;

  @Autowired
  private WalletRepository walletRepository;

  @Autowired
  private MemberRepository memberRepository;

  private Long memberId;
  private Long walletId;

  @BeforeEach
  void setUp() {
    walletRepository.deleteAll();
    memberRepository.deleteAll();

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
  @DisplayName("동시 출금 테스트 - 동시성 문제로 인한 잔액 무결성이 보장되지 않는 경우")
  void concurrentWithdraw_withoutConcurrencyControl() throws Exception {
    int threadCount = 100;
    long withdrawAmount = 1000L;
    long initialBalance = 10_000L;

    // 작업 병렬 처리를 위한 스레드 풀 생성
    ExecutorService executorService = Executors.newFixedThreadPool(30);

    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger failCount = new AtomicInteger();
    List<Throwable> unexpectedErrors = new CopyOnWriteArrayList<>();

    for (int i = 0; i < threadCount; i++) {
      final String transactionId = "TXN-" + i;

      executorService.submit(() -> {
        try {
          startLatch.await();

          WalletTransactionCommand command = new WalletTransactionCommand(memberId, walletId,
            withdrawAmount, transactionId, WalletRequestType.WITHDRAW);

          try {
            walletTransactionService.withdraw(command);
            successCount.incrementAndGet();
          } catch (GeneralException e) {
            failCount.incrementAndGet();
          } catch (Throwable t) {
            unexpectedErrors.add(t);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          unexpectedErrors.add(e);
        } finally {
          doneLatch.countDown();
        }
      });
    }

    startLatch.countDown(); // 동시에 시작
    doneLatch.await();      // 전부 끝날 때까지 대기

    executorService.shutdown(); // 스레드풀 종료

    // 최대 60초 동안 종료 대기. 안 끝나면 강제 종료
    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
      executorService.shutdownNow();
    }

    Wallet wallet = walletRepository.findById(walletId).orElseThrow();
    long finalBalance = wallet.getBalance();
    long expectedSuccessCount = initialBalance / withdrawAmount;

    log.debug(
      "Test Result - successCount = {}, failCount = {}, finalBalance = {}, expectedSuccessCount = {}",
      successCount.get(), failCount.get(), finalBalance, expectedSuccessCount);

    assertThat(unexpectedErrors).isEmpty();
    assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
    assertThat(successCount.get()).isGreaterThanOrEqualTo((int) expectedSuccessCount);
  }
}
