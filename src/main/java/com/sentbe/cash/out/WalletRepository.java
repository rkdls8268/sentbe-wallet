package com.sentbe.cash.out;

import com.sentbe.cash.domain.Wallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

  Optional<Wallet> findByMemberId(Long memberId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
        select w
        from Wallet w
        where w.id = :walletId
    """)
  Optional<Wallet> findByIdForUpdate(@Param("walletId") Long walletId);
}
