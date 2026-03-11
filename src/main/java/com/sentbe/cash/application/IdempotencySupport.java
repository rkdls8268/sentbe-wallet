package com.sentbe.cash.application;

import com.sentbe.cash.domain.IdempotencyRecord;
import com.sentbe.cash.domain.IdempotencyStatus;
import com.sentbe.cash.domain.exception.IdempotencyConflictException;
import com.sentbe.cash.domain.exception.InsufficientBalanceException;
import com.sentbe.cash.domain.exception.InvalidAmountException;
import com.sentbe.cash.in.dto.WalletTransactionCommand;
import com.sentbe.cash.in.dto.WalletTransactionResponse;
import com.sentbe.cash.out.IdempotencyRecordRepository;
import com.sentbe.global.exception.GeneralException;
import com.sentbe.global.json.JsonConverter;
import com.sentbe.global.status.ErrorStatus;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencySupport {
  private final IdempotencyRecordRepository idempotencyRecordRepository;
  private final IdempotencyRecordService idempotencyRecordService;
  private final JsonConverter jsonConverter;

  public WalletTransactionResponse execute(
    WalletTransactionCommand command,
    Function<WalletTransactionCommand, WalletTransactionResponse> action
  ) {
    // 1. idempotency key 선점
    Optional<IdempotencyRecord> existing =
      idempotencyRecordRepository.findByMemberIdAndTransactionId(
        command.memberId(),
        command.transactionId()
      );

    // 2. 이미 있으면 기존 결과 반환
    if (existing.isPresent()) {
      return restoreOrThrow(existing.get(), command);
    }

    IdempotencyRecord record = createProcessingOrRestore(command);

    try {
      // 3. wallet row를 비관적 락으로 조회
      // 4. 잔액 확인
      // 5. balance 차감
      // 6. cash_log insert
      WalletTransactionResponse response = action.apply(command);

      // 7-1. idempotency SUCCESS 저장
      saveSuccess(record.getId(), response);
      return response;

      // 7-2. idempotency FAILED 저장
    } catch (InsufficientBalanceException e) {
      saveFailure(record.getId(), "INSUFFICIENT_BALANCE", e.getMessage());
      throw new GeneralException(ErrorStatus.INSUFFICIENT_BALANCE);
    } catch (InvalidAmountException e) {
      saveFailure(record.getId(), "INVALID_AMOUNT", e.getMessage());
      throw new GeneralException(ErrorStatus.INVALID_AMOUNT);
    } catch (RuntimeException e) {
      saveFailure(record.getId(), "INTERNAL_SERVER_ERROR", e.getMessage());
      throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private WalletTransactionResponse restoreOrThrow(
    IdempotencyRecord record,
    WalletTransactionCommand command
  ) {
    try {
      record.validateSameRequest(command.requestType(), command.amount());
    } catch (IdempotencyConflictException e) {
      throw new GeneralException(ErrorStatus.TRANSACTION_CONFLICT);
    }

    return restoreResponse(record);
  }

  private IdempotencyRecord createProcessingOrRestore(WalletTransactionCommand command) {
    try {
      return idempotencyRecordService.createProcessing(
        command.memberId(),
        command.transactionId(),
        command.requestType(),
        command.amount()
      );
    } catch (DataIntegrityViolationException e) {
      IdempotencyRecord existingRecord = idempotencyRecordRepository
        .findByMemberIdAndTransactionIdForUpdate(
          command.memberId(),
          command.transactionId()
        )
        .orElseThrow(() -> new GeneralException(ErrorStatus.IDEMPOTENCY_RECORD_NOT_FOUND));

      existingRecord.validateSameRequest(command.requestType(), command.amount());
      return existingRecord;
    }
  }

  private void saveSuccess(Long recordId, WalletTransactionResponse response) {
    idempotencyRecordService.markSuccess(
      recordId,
      "OK",
      "정상 처리되었습니다.",
      jsonConverter.toJson(response)
    );
  }

  private void saveFailure(Long recordId, String code, String message) {
    idempotencyRecordService.markFailed(
      recordId,
      code,
      message,
      null
    );
  }

  private WalletTransactionResponse restoreResponse(IdempotencyRecord record) {
    if (IdempotencyStatus.PROCESSING.equals(record.getStatus())) {
      throw new GeneralException(ErrorStatus.DUPLICATE_REQUEST_IN_PROGRESS);
    }

    if (IdempotencyStatus.FAILED.equals(record.getStatus())) {
      throw mapToGeneralException(record.getResponseCode());
    }

    // status == SUCCESS
    WalletTransactionResponse response =
      jsonConverter.fromJson(record.getResponseBody(), WalletTransactionResponse.class);
    return response;
  }

  private GeneralException mapToGeneralException(String code) {
    return switch (code) {
      case "INSUFFICIENT_BALANCE" -> new GeneralException(ErrorStatus.INSUFFICIENT_BALANCE);
      case "WALLET_NOT_FOUND" -> new GeneralException(ErrorStatus.WALLET_NOT_FOUND);
      case "INVALID_AMOUNT" -> new GeneralException(ErrorStatus.INVALID_AMOUNT);
      default -> new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
    };
  }

}
