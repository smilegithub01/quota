package com.bank.quota.core.repository;

import com.bank.quota.core.domain.TccTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TccTransactionLogRepository extends JpaRepository<TccTransactionLog, Long> {

    Optional<TccTransactionLog> findByXid(String xid);

    List<TccTransactionLog> findByStatus(TccTransactionLog.TransactionStatus status);

    @Query("SELECT t FROM TccTransactionLog t WHERE t.status = 'TRY' AND t.createTime < :timeout")
    List<TccTransactionLog> findTimeoutTransactions(@Param("timeout") LocalDateTime timeout);

    @Query("SELECT t FROM TccTransactionLog t WHERE t.status = :status ORDER BY t.createTime DESC")
    List<TccTransactionLog> findByStatusOrderByCreateTimeDesc(@Param("status") TccTransactionLog.TransactionStatus status);

    boolean existsByXid(String xid);
}
