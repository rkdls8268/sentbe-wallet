package com.sentbe.cash.out;

import com.sentbe.cash.domain.IdempotencyRecord;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

  Optional<IdempotencyRecord> findByMemberIdAndTransactionId(Long memberId, String transactionId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
        select i
        from IdempotencyRecord i
        where i.memberId = :memberId
          and i.transactionId = :transactionId
    """)
  Optional<IdempotencyRecord> findByMemberIdAndTransactionIdForUpdate(
    @Param("memberId") Long memberId,
    @Param("transactionId") String transactionId
  );
}
