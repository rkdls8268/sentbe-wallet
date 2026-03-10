package com.sentbe.cash.application;

import com.sentbe.cash.domain.CashLog;
import com.sentbe.cash.domain.IdempotencyRecord;
import com.sentbe.cash.domain.IdempotencyStatus;
import com.sentbe.cash.domain.Member;
import com.sentbe.cash.domain.Wallet;
import com.sentbe.cash.domain.WalletRequestType;
import com.sentbe.cash.domain.exception.DuplicateRequestInProgressException;
import com.sentbe.cash.domain.exception.IdempotencyConflictException;
import com.sentbe.cash.domain.exception.InsufficientBalanceException;
import com.sentbe.cash.in.dto.CashLogResponse;
import com.sentbe.cash.in.dto.CashRequest;
import com.sentbe.cash.in.dto.MemberDto;
import com.sentbe.cash.in.dto.WalletTransactionResponse;
import com.sentbe.cash.out.CashLogRepository;
import com.sentbe.cash.out.IdempotencyRecordRepository;
import com.sentbe.cash.out.MemberRepository;
import com.sentbe.cash.out.WalletRepository;
import com.sentbe.global.exception.GeneralException;
import com.sentbe.global.json.JsonConverter;
import com.sentbe.global.status.ErrorStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

  private final WalletRepository walletRepository;
  private final MemberRepository memberRepository;
  private final CashLogRepository cashLogRepository;
  private final IdempotencyRecordRepository idempotencyRecordRepository;
  private final IdempotencyRecordService idempotencyRecordService;
  private final JsonConverter jsonConverter;

  public void createWallet(MemberDto memberDto) {
    Member member = memberRepository.getReferenceById(memberDto.id());
    Wallet wallet = Wallet.create(member);
    walletRepository.save(wallet);
  }

  public Wallet getWalletByMember(Long memberId) {
    return walletRepository.findByMemberId(memberId)
      .orElseThrow(() -> new GeneralException(ErrorStatus.WALLET_NOT_FOUND));
  }

  public List<CashLogResponse> getWallets(Long walletId) {
    boolean exists = walletRepository.existsById(walletId);
    if (!exists) {
      throw new GeneralException(ErrorStatus.WALLET_NOT_FOUND);
    }

    List<CashLog> cashLogs = cashLogRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    return cashLogs.stream().map(CashLog::toResponse).toList();
  }

  @Transactional
  public WalletTransactionResponse deposit(Long walletId, CashRequest request) {
    // 입금
    return executeWithIdempotency(request.memberId(), walletId, request.amount(),
      request.transactionId(), WalletRequestType.DEPOSIT);
  }

  @Transactional
  public WalletTransactionResponse withdraw(Long walletId, CashRequest request) {
    // 출금
    return executeWithIdempotency(request.memberId(), walletId, request.amount(),
      request.transactionId(), WalletRequestType.WITHDRAW);
  }

  private WalletTransactionResponse executeWithIdempotency(
    Long memberId,
    Long walletId,
    Long amount,
    String transactionId,
    WalletRequestType requestType
  ) {
    validateAmount(amount);

    Optional<IdempotencyRecord> existing =
      idempotencyRecordRepository.findByMemberIdAndTransactionId(memberId, transactionId);

    if (existing.isPresent()) {
      IdempotencyRecord record = existing.get();
      try {
        record.validateSameRequest(requestType, amount);
      } catch (IdempotencyConflictException e) {
        throw new GeneralException(ErrorStatus.TRANSACTION_CONFLICT);
      }
      return restoreResponse(record);
    }

    IdempotencyRecord record;
    try {
      record = idempotencyRecordService.createProcessing(memberId, transactionId, requestType, amount);
    } catch (DataIntegrityViolationException e) {
      IdempotencyRecord existingRecord = idempotencyRecordRepository
        .findByMemberIdAndTransactionIdForUpdate(memberId, transactionId)
        .orElseThrow();

      existingRecord.validateSameRequest(requestType, amount);
      return restoreResponse(existingRecord);
    }

    try {
      Wallet wallet = walletRepository.findById(walletId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.WALLET_NOT_FOUND));
      // todo
//      Wallet wallet = walletRepository.findByMemberIdForUpdate(memberId)
//        .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));


      if (requestType == WalletRequestType.DEPOSIT) {
        wallet.credit(amount, transactionId);
      } else {
        wallet.debit(amount, transactionId);
      }

      WalletTransactionResponse response = WalletTransactionResponse.of(
        transactionId,
        wallet.getId(),
        wallet.getBalance()
      );
      idempotencyRecordService.markSuccess(record.getId(), "OK", "정상 처리되었습니다.", jsonConverter.toJson(response));
      return response;

    } catch (InsufficientBalanceException e) {
      idempotencyRecordService.markFailed(record.getId(), "INSUFFICIENT_BALANCE", e.getMessage(), null);
      throw new GeneralException(ErrorStatus.INSUFFICIENT_BALANCE);

    } catch (RuntimeException e) {
      idempotencyRecordService.markFailed(record.getId(), "INTERNAL_SERVER_ERROR", e.getMessage(), null);
      throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private WalletTransactionResponse restoreResponse(IdempotencyRecord record) {
    if (record.getStatus() == IdempotencyStatus.SUCCESS ||
      record.getStatus() == IdempotencyStatus.FAILED) {
      return jsonConverter.fromJson(record.getResponseBody(), WalletTransactionResponse.class);
    }

    throw new DuplicateRequestInProgressException("동일 요청이 현재 처리 중입니다.");
  }

  private void validateAmount(Long amount) {
    if (amount == null || amount <= 0) {
      throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
    }
  }

}
