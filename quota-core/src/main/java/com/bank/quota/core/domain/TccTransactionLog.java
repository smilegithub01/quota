package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "tcc_transaction_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TccTransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "xid", unique = true, nullable = false, length = 128)
    private String xid;

    @Column(name = "transaction_name", nullable = false, length = 100)
    private String transactionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "lock_results", columnDefinition = "TEXT")
    private String lockResults;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public enum TransactionStatus {
        TRY,
        CONFIRMED,
        CANCELLED
    }
}
