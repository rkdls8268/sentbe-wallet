package com.sentbe.cash.out;

import com.sentbe.cash.domain.Wallet;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

  Optional<Wallet> findByMemberId(Long memberId);
}
