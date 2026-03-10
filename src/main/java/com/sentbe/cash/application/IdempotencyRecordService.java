package com.sentbe.cash.application;

import com.sentbe.cash.domain.IdempotencyRecord;
import com.sentbe.cash.domain.WalletRequestType;
import com.sentbe.cash.out.IdempotencyRecordRepository;
import com.sentbe.global.exception.GeneralException;
import com.sentbe.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdempotencyRecordService {

  private final IdempotencyRecordRepository idempotencyRecordRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public IdempotencyRecord createProcessing(
    Long memberId,
    String transactionId,
    WalletRequestType requestType,
    Long amount
  ) {
    return idempotencyRecordRepository.save(
      IdempotencyRecord.processing(memberId, transactionId, requestType, amount)
    );
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markSuccess(
    Long recordId,
    String code,
    String message,
    String responseBody
  ) {
    IdempotencyRecord record = idempotencyRecordRepository.findById(recordId)
      .orElseThrow(() -> new GeneralException(ErrorStatus.IDEMPOTENCY_RECORD_NOT_FOUND));
    record.markSuccess(code, message, responseBody);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markFailed(
    Long recordId,
    String code,
    String message,
    String responseBody
  ) {
    IdempotencyRecord record = idempotencyRecordRepository.findById(recordId)
      .orElseThrow(() -> new GeneralException(ErrorStatus.IDEMPOTENCY_RECORD_NOT_FOUND));
    record.markFailed(code, message, responseBody);
  }
}
