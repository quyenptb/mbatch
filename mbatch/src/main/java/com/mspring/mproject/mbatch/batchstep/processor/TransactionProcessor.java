package com.mspring.mproject.mbatch.batchstep.processor;

import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@StepScope
public class TransactionProcessor implements ItemProcessor<TransactionRecord, TransactionRecord> {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessor.class);

    private final String processingDate;

    private LocalDate expectedDate;

    public TransactionProcessor(@Value("#{jobParameters['processingDate']}") String processingDate) {
        this.processingDate = processingDate;
    }

    @PostConstruct
    public void init() {
        this.expectedDate = LocalDate.parse(processingDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        log.info("[PROCESSOR] Đã khởi tạo. Sẵn sàng lọc cho ngày: {}", this.expectedDate);
    }

    @Override
    public TransactionRecord process(TransactionRecord item) throws Exception {

        if (!item.getTransactionDate().equals(this.expectedDate)) {
            return null; //skip this row
        }


        //business logic 1: check valid balance
        if (item.getUpdatedBalance().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("[PROCESSOR] ID: {} FAILED. Số dư bị âm.", item.getTransactionId());
            throw new IllegalArgumentException("Updated balance cannot be negative for Transaction ID: " + item.getTransactionId());
        }

        //business logic 2: check anomaly
        if (item.getTransactionAmount().compareTo(item.getTotalBalance().multiply(new BigDecimal("0.5"))) > 0) {
            item.setIsAnomaly(true);
        }

        return item;
    }
}