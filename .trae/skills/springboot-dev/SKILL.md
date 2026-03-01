---
name: "springboot-dev"
description: "Expert Spring Boot development for banking/enterprise systems. Invoke when user asks to create Spring Boot apps, add features, or implement banking functionality."
---

# Spring Boot Enterprise Development

This skill provides expert-level Spring Boot development guidance for enterprise banking systems. Follow these principles for all development tasks.

## Core Architecture Principles

### Domain-Driven Design (DDD)
- Implement bounded contexts with clear domain boundaries
- Use aggregates, entities, and value objects appropriately
- Apply repository pattern for data access abstraction
- Implement domain events for cross-context communication

### Layered Architecture
```
├── controller/     # REST API endpoints
├── service/        # Business logic layer
├── domain/         # Domain models and business rules
├── repository/     # Data access layer
├── dto/            # Data transfer objects
└── config/         # Configuration classes
```

## Banking-Specific Requirements

### Core Banking System Standards & Compliance
- Follow banking core system development standards (ISO 20022, SWIFT, PCI-DSS)
- Implement regulatory compliance (Basel III, AML/KYC, GDPR, local financial regulations)
- Adhere to banking data exchange protocols and message formats
- Implement comprehensive audit trails for all financial operations
- Design for regulatory reporting capabilities (daily, monthly, annual reports)
- Implement data retention policies per regulatory requirements

### Financial Data Security & Compliance
- Encrypt sensitive financial data at rest using AES-256 or stronger
- Implement TLS 1.3 for data in transit
- Use field-level encryption for PII and financial data
- Implement secure key management with rotation policies
- Design for PCI-DSS compliance if handling card data
- Implement comprehensive access logging and monitoring
- Use tokenization for sensitive data display and storage
- Implement data masking for non-production environments

### Professional Monetary Amount Processing
- **NEVER use float/double for monetary calculations** - use `BigDecimal` exclusively
- Implement precise decimal arithmetic with appropriate scale (typically 2-4 decimal places)
- Define consistent rounding rules (banker's rounding, half-up, etc.) per business requirements
- Use `RoundingMode` enum for explicit rounding control
- Implement currency-aware calculations for multi-currency support
- Handle large transaction amounts with proper overflow checks
- Implement amount validation with min/max limits per transaction type
- Use Money pattern or dedicated Amount value objects for type safety
- Store amounts in smallest currency unit (cents) or with decimal precision as required

```java
public class MonetaryUtils {
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    
    public static BigDecimal calculateInterest(BigDecimal principal, BigDecimal rate, int days) {
        return principal.multiply(rate)
            .multiply(BigDecimal.valueOf(days))
            .divide(BigDecimal.valueOf(365), SCALE, ROUNDING_MODE);
    }
    
    public static BigDecimal roundToCurrency(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
```

### Credit Business Process & Risk Control Models
- Implement credit lifecycle management (application, approval, disbursement, repayment, closure)
- Design credit scoring and rating models integration points
- Implement risk-based quota calculation with multi-tier risk levels (R1-R5)
- Design for credit limit management and utilization tracking
- Implement collateral management and valuation mechanisms
- Design for interest calculation (simple, compound, amortized)
- Implement overdue management and collection workflows
- Design for credit event tracking and status transitions
- Implement risk monitoring and early warning systems
- Design for regulatory reporting (LDR, NPL ratios, etc.)

### Banking Business Logic & Regulatory Requirements
- Implement double-entry bookkeeping principles for all financial transactions
- Design for transaction atomicity with proper rollback mechanisms
- Implement comprehensive reconciliation processes
- Design for end-of-day (EOD) batch processing and settlement
- Implement real-time balance updates and availability checks
- Design for multi-tenant customer data isolation
- Implement transaction limits and velocity controls
- Design for fraud detection and prevention integration
- Implement comprehensive business rule validation
- Design for audit trail with immutable records

### Security & Compliance
- Implement Spring Security with JWT authentication
- Use role-based access control (RBAC) with fine-grained permissions
- Implement multi-factor authentication for critical operations
- Encrypt sensitive data at rest (AES-256) and in transit (TLS)
- Implement comprehensive audit logging for all financial operations
- Validate all inputs against business rules and regulatory constraints
- Implement session management with timeout and concurrent session limits
- Design for security incident response and monitoring

### Transaction Management
- Use `@Transactional` with appropriate isolation levels
- Implement idempotency for critical operations
- Use optimistic locking for concurrent updates
- Design compensation mechanisms for distributed transactions

### Data Integrity
- Implement double-entry bookkeeping for financial transactions
- Use database constraints and triggers for data validation
- Design for ACID compliance in critical paths
- Implement reconciliation mechanisms

## Code Quality Standards

### Naming Conventions
- Classes: PascalCase (e.g., `QuotaService`, `TransactionController`)
- Methods: camelCase, descriptive verbs (e.g., `calculateQuota`, `validateTransaction`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_QUOTA_LIMIT`)
- Packages: lowercase, hierarchical (e.g., `com.bank.quota.service`)

### Code Structure
- Keep classes under 300 lines
- Methods under 50 lines
- Maximum 5 parameters per method
- Use DTOs for API contracts
- Implement proper exception handling with custom exceptions

### Error Handling
- Create custom exception classes extending `RuntimeException`
- Use `@ControllerAdvice` for global exception handling
- Return consistent error response structure
- Log errors with appropriate severity levels

## Best Practices

### Performance
- Use connection pooling (HikariCP)
- Implement caching where appropriate (Redis, Caffeine)
- Optimize database queries with proper indexing
- Use pagination for large datasets
- Implement async processing for non-critical operations

### Testing
- Unit tests for business logic (JUnit 5, Mockito)
- Integration tests for API endpoints (TestContainers)
- Test coverage >80% for critical paths
- Test edge cases and error scenarios

### Configuration
- Use `application.yml` for environment-specific configs
- Implement configuration properties with `@ConfigurationProperties`
- Use Spring profiles for dev/test/prod environments
- Externalize sensitive configuration (environment variables, vaults)

## Common Patterns

### Monetary Amount Pattern (CRITICAL - Always Use BigDecimal)
```java
public class Money implements Serializable {
    private final BigDecimal amount;
    private final Currency currency;
    
    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
        this.currency = Objects.requireNonNull(currency);
    }
    
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }
    
    public static Money of(double amount, String currencyCode) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance(currencyCode));
    }
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }
    
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public String toFormattedString() {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(currency);
        return format.format(amount);
    }
}
```

### Credit Quota Calculation Pattern
```java
@Service
@Transactional
@RequiredArgsConstructor
public class CreditQuotaService {
    private final QuotaRepository quotaRepository;
    private final RiskAssessmentService riskAssessmentService;
    private final CustomerRepository customerRepository;
    
    public QuotaDTO calculateQuota(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        RiskLevel riskLevel = riskAssessmentService.assessRisk(customer);
        
        BigDecimal baseQuota = getBaseQuotaByRiskLevel(riskLevel);
        BigDecimal adjustedQuota = adjustQuotaByCustomerFactors(customer, baseQuota);
        
        Quota quota = Quota.builder()
            .customerId(customerId)
            .riskLevel(riskLevel)
            .totalQuota(adjustedQuota)
            .availableQuota(adjustedQuota)
            .status(QuotaStatus.ACTIVE)
            .build();
        
        return QuotaDTO.fromEntity(quotaRepository.save(quota));
    }
    
    private BigDecimal getBaseQuotaByRiskLevel(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case R1 -> new BigDecimal("1000000.00");
            case R2 -> new BigDecimal("800000.00");
            case R3 -> new BigDecimal("500000.00");
            case R4 -> new BigDecimal("300000.00");
            case R5 -> new BigDecimal("100000.00");
        };
    }
    
    private BigDecimal adjustQuotaByCustomerFactors(Customer customer, BigDecimal baseQuota) {
        BigDecimal multiplier = BigDecimal.ONE;
        
        if (customer.getAnnualIncome().compareTo(new BigDecimal("500000.00")) > 0) {
            multiplier = multiplier.add(new BigDecimal("0.2"));
        }
        
        if (customer.getCreditScore() > 750) {
            multiplier = multiplier.add(new BigDecimal("0.1"));
        }
        
        return baseQuota.multiply(multiplier).setScale(2, RoundingMode.HALF_EVEN);
    }
}
```

### Transaction Processing with Double-Entry Pattern
```java
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AuditLogService auditLogService;
    
    public TransactionDTO processTransaction(TransactionRequest request) {
        Account debitAccount = accountRepository.findById(request.getDebitAccountId())
            .orElseThrow(() -> new AccountNotFoundException(request.getDebitAccountId()));
        
        Account creditAccount = accountRepository.findById(request.getCreditAccountId())
            .orElseThrow(() -> new AccountNotFoundException(request.getCreditAccountId()));
        
        validateTransaction(request, debitAccount, creditAccount);
        
        Money amount = Money.of(request.getAmount(), request.getCurrency());
        
        Transaction debitEntry = createTransactionEntry(debitAccount, amount, TransactionType.DEBIT, request);
        Transaction creditEntry = createTransactionEntry(creditAccount, amount, TransactionType.CREDIT, request);
        
        updateAccountBalances(debitAccount, creditAccount, amount);
        
        transactionRepository.saveAll(List.of(debitEntry, creditEntry));
        
        auditLogService.logTransaction(debitEntry, creditEntry);
        
        return TransactionDTO.fromEntity(debitEntry);
    }
    
    private void validateTransaction(TransactionRequest request, Account debitAccount, Account creditAccount) {
        if (!debitAccount.getCurrency().equals(creditAccount.getCurrency())) {
            throw new CurrencyMismatchException("Currency mismatch between accounts");
        }
        
        if (debitAccount.getAvailableBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in debit account");
        }
        
        if (request.getAmount().compareTo(new BigDecimal("0.00")) <= 0) {
            throw new InvalidAmountException("Transaction amount must be positive");
        }
    }
    
    private void updateAccountBalances(Account debitAccount, Account creditAccount, Money amount) {
        debitAccount.setAvailableBalance(
            debitAccount.getAvailableBalance().subtract(amount.getAmount())
                .setScale(2, RoundingMode.HALF_EVEN)
        );
        
        creditAccount.setAvailableBalance(
            creditAccount.getAvailableBalance().add(amount.getAmount())
                .setScale(2, RoundingMode.HALF_EVEN)
        );
        
        accountRepository.saveAll(List.of(debitAccount, creditAccount));
    }
}
```

### Service Pattern
```java
@Service
@Transactional
public class QuotaService {
    private final QuotaRepository quotaRepository;
    
    public QuotaDTO calculateQuota(CalculateQuotaRequest request) {
        // Business logic
    }
}
```

### Controller Pattern
```java
@RestController
@RequestMapping("/api/v1/quotas")
@RequiredArgsConstructor
public class QuotaController {
    private final QuotaService quotaService;
    
    @PostMapping("/calculate")
    public ResponseEntity<QuotaDTO> calculateQuota(@Valid @RequestBody CalculateQuotaRequest request) {
        return ResponseEntity.ok(quotaService.calculateQuota(request));
    }
}
```

### Repository Pattern
```java
@Repository
public interface QuotaRepository extends JpaRepository<Quota, Long> {
    Optional<Quota> findByCustomerIdAndRiskLevel(Long customerId, RiskLevel riskLevel);
    
    @Query("SELECT q FROM Quota q WHERE q.customerId = :customerId AND q.status = 'ACTIVE'")
    List<Quota> findActiveQuotasByCustomer(@Param("customerId") Long customerId);
}
```

## When to Apply This Skill

Invoke this skill when:
- Creating new Spring Boot applications or modules
- Implementing banking/financial features (quotas, transactions, accounts)
- Adding REST API endpoints
- Implementing security and authentication
- Designing database schemas and repositories
- Writing business logic for financial operations
- Setting up configuration and dependency injection
- Implementing transaction management
- Creating DTOs and request/response models
- Adding validation and error handling

## Dependencies to Consider

```xml
<!-- Core Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
</dependency>

<!-- Caching -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Banking & Financial -->
<dependency>
    <groupId>org.javamoney</groupId>
    <artifactId>moneta</artifactId>
    <version>1.4.4</version>
</dependency>
<dependency>
    <groupId>javax.money</groupId>
    <artifactId>money-api</artifactId>
    <version>1.1</version>
</dependency>

<!-- Security & Encryption -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.70</version>
</dependency>

<!-- API Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

## Validation Checklist

Before completing any task, verify:

### Architecture & Code Quality
- [ ] Code follows DDD principles and layered architecture
- [ ] Code follows naming conventions and style guidelines
- [ ] Unit tests cover critical business logic (>80% coverage)
- [ ] API contracts are well-defined with OpenAPI/Swagger

### Banking System Standards
- [ ] Implementation follows banking core system development standards
- [ ] Regulatory compliance requirements are met (Basel III, AML/KYC, etc.)
- [ ] Banking data exchange protocols are properly implemented
- [ ] Comprehensive audit trails are implemented for all financial operations
- [ ] Data retention policies are implemented per regulatory requirements

### Financial Data Security
- [ ] Sensitive financial data is encrypted at rest (AES-256 or stronger)
- [ ] TLS 1.3 is implemented for data in transit
- [ ] Field-level encryption is used for PII and financial data
- [ ] Secure key management with rotation policies is implemented
- [ ] PCI-DSS compliance is ensured if handling card data
- [ ] Comprehensive access logging and monitoring is implemented
- [ ] Tokenization is used for sensitive data display and storage
- [ ] Data masking is implemented for non-production environments

### Monetary Amount Processing
- [ ] **CRITICAL**: BigDecimal is used for ALL monetary calculations (NO float/double)
- [ ] Decimal arithmetic uses appropriate scale (2-4 decimal places)
- [ ] Rounding rules are explicitly defined and consistently applied
- [ ] RoundingMode enum is used for explicit rounding control
- [ ] Currency-aware calculations are implemented for multi-currency support
- [ ] Large transaction amounts have proper overflow checks
- [ ] Amount validation includes min/max limits per transaction type
- [ ] Money pattern or Amount value objects are used for type safety
- [ ] Amounts are stored with appropriate precision (cents or decimal)

### Credit Business Logic
- [ ] Credit lifecycle management is properly implemented
- [ ] Credit scoring and rating models integration points are designed
- [ ] Risk-based quota calculation supports multi-tier risk levels (R1-R5)
- [ ] Credit limit management and utilization tracking is implemented
- [ ] Collateral management and valuation mechanisms are designed
- [ ] Interest calculation methods are correctly implemented
- [ ] Overdue management and collection workflows are designed
- [ ] Credit event tracking and status transitions are implemented
- [ ] Risk monitoring and early warning systems are in place
- [ ] Regulatory reporting requirements (LDR, NPL ratios) are met

### Security & Transaction Management
- [ ] Security measures are implemented (authentication, authorization, MFA)
- [ ] Transaction boundaries are properly defined with appropriate isolation levels
- [ ] Idempotency is implemented for critical operations
- [ ] Double-entry bookkeeping principles are followed
- [ ] Reconciliation mechanisms are implemented
- [ ] EOD batch processing and settlement is designed

### Input Validation & Error Handling
- [ ] Input validation is comprehensive against business rules
- [ ] Error handling is consistent and informative
- [ ] Transaction limits and velocity controls are implemented
- [ ] Fraud detection and prevention integration points are designed

### Operational Readiness
- [ ] Logging is appropriate for production monitoring
- [ ] Sensitive data is properly encrypted/protected
- [ ] Session management with timeout is implemented
- [ ] Security incident response and monitoring is designed
