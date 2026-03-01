package com.bank.quota.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Column(name = "currency_name", nullable = false, length = 50)
    private String currencyName;

    @Column(name = "rate_to_cny", nullable = false, precision = 20, scale = 8)
    private BigDecimal rateToCny;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_source", nullable = false, length = 50)
    private RateSource rateSource;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "effective_time", nullable = false)
    private LocalDateTime effectiveTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RateStatus status = RateStatus.ACTIVE;

    @Column(name = "create_by", length = 50)
    private String createBy;

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    public enum RateSource {
        REUTERS,
        PBOC,
        MANUAL
    }

    public enum RateStatus {
        ACTIVE,
        INACTIVE
    }
}
