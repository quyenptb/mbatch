package com.mspring.mproject.mbatch.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Getter
@Setter
public class TransactionRecord {
    private String batchId;

    @Id
    private Long transactionId;
    private Long customerId;
    private String accountType;
    private BigDecimal totalBalance;
    private BigDecimal transactionAmount;
    private BigDecimal updatedBalance;
    private BigDecimal investmentAmount;
    private String investmentType;
    private Boolean isAnomaly;
    private LocalDate transactionDate;
    private Integer year;
    private Integer month;
    private Integer day;
    private LocalDateTime processedAt;
}
