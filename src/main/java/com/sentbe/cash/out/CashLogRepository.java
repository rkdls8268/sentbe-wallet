package com.sentbe.cash.out;

import com.sentbe.cash.domain.CashLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashLogRepository extends JpaRepository<CashLog, Long> {

  List<CashLog> findByWalletIdOrderByCreatedAtDesc(Long walletId);

}
