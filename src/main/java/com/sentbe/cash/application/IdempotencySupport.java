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
import org.springframework.dao.CannotAcquireLockException;
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

    // 3. 없으면 PROCESSING 생성 시도 후 동시 요청일 시 기존 레코드 복구
    IdempotencyRecord record = createProcessingOrRestore(command);

    // 4. 복구된 레코드가 이미 처리 완료/실패 상태라면 기존 결과 사용
    if (!IdempotencyStatus.PROCESSING.equals(record.getStatus())) {
      return restoreOrThrow(record, command);
    }
    WalletTransactionResponse response;
    try {
      // 5. transaction 처리 & cash_log insert
      response = action.apply(command);
      // 6-1. idempotency FAILED 저장
    } catch (InsufficientBalanceException e) {
      saveFailureSafely(record.getId(), "INSUFFICIENT_BALANCE", e.getMessage());
      throw new GeneralException(ErrorStatus.INSUFFICIENT_BALANCE);
    } catch (InvalidAmountException e) {
      saveFailureSafely(record.getId(), "INVALID_AMOUNT", e.getMessage());
      throw new GeneralException(ErrorStatus.INVALID_AMOUNT);
    } catch (RuntimeException e) {
      saveFailureSafely(record.getId(), "INTERNAL_SERVER_ERROR", e.getMessage());
      throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
    }
    try {
      // 6-2. idempotency SUCCESS 저장
      saveSuccess(record.getId(), response);
      return response;
    } catch (CannotAcquireLockException e) {
      throw new GeneralException(ErrorStatus.IDEMPOTENCY_SAVE_FAILED);
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
      return recoverExistingRecord(command);
    }
  }

  /**
   * 이미 동일한 (memberId, transactionId) 레코드가 생성된 경우 복구
   * 우선 for update 로 조회를 시도하고,
   * 락 획득 실패 시 일반 조회로 상태를 재판단한다.
   */
  private IdempotencyRecord recoverExistingRecord(WalletTransactionCommand command) {
    try {
      IdempotencyRecord existingRecord = idempotencyRecordRepository
        .findByMemberIdAndTransactionIdForUpdate(
          command.memberId(),
          command.transactionId()
        )
        .orElseThrow(() -> new GeneralException(ErrorStatus.IDEMPOTENCY_RECORD_NOT_FOUND));

      existingRecord.validateSameRequest(command.requestType(), command.amount());
      return existingRecord;

    } catch (CannotAcquireLockException e) {
      return recoverAfterLockFailure(command);
    }
  }

  /**
   * for update 락 획득에 실패한 경우 일반 조회로 상태를 다시 확인한다.
   * - SUCCESS    -> 기존 성공 응답 복원 가능
   * - FAILED     -> 기존 실패 응답 복원 가능
   * - PROCESSING -> 아직 다른 요청이 처리 중
   */
  private IdempotencyRecord recoverAfterLockFailure(WalletTransactionCommand command) {
    IdempotencyRecord record = idempotencyRecordRepository
      .findByMemberIdAndTransactionId(
        command.memberId(),
        command.transactionId()
      )
      .orElseThrow(() -> new GeneralException(ErrorStatus.IDEMPOTENCY_RECORD_NOT_FOUND));

    record.validateSameRequest(command.requestType(), command.amount());

    if (IdempotencyStatus.SUCCESS.equals(record.getStatus())) {
      return record;
    }

    if (IdempotencyStatus.FAILED.equals(record.getStatus())) {
      return record;
    }

    if (IdempotencyStatus.PROCESSING.equals(record.getStatus())) {
      throw new GeneralException(ErrorStatus.DUPLICATE_REQUEST_IN_PROGRESS);
    }

    throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
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

  private void saveFailureSafely(Long recordId, String code, String message) {
    try {
      saveFailure(recordId, code, message);
    } catch (CannotAcquireLockException e) {
      throw new GeneralException(ErrorStatus.IDEMPOTENCY_SAVE_FAILED);
    }
  }

  private WalletTransactionResponse restoreResponse(IdempotencyRecord record) {
    if (IdempotencyStatus.PROCESSING.equals(record.getStatus())) {
      throw new GeneralException(ErrorStatus.DUPLICATE_REQUEST_IN_PROGRESS);
    }

    if (IdempotencyStatus.FAILED.equals(record.getStatus())) {
      throw mapToGeneralException(record.getResponseCode());
    }

    if (IdempotencyStatus.SUCCESS.equals(record.getStatus())) {
      return jsonConverter.fromJson(record.getResponseBody(), WalletTransactionResponse.class);
    }

    throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR);
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
