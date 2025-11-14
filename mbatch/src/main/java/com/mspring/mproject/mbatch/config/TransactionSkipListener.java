package com.mspring.mproject.mbatch.config;


import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionSkipListener implements SkipListener<TransactionRecord, TransactionRecord> {

    private static final Logger log = LoggerFactory.getLogger(TransactionSkipListener.class);

    @Autowired
    private BatchMetricsService metricsService;

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("READ ERROR");
        log.warn("SKIPPED IN READ - REASON: {}", t.getMessage());

        metricsService.incrementSkipReadCount();
    }

    @Override
    public void onSkipInProcess(TransactionRecord item, Throwable t) {
        log.warn("PROCESS ERROR");
        log.warn("SKIPPED IN PROCESS - ID: {}", (item != null ? item.getTransactionId() : "UNKNOWN"));
        log.warn("REASON: {}", t.getMessage());

        metricsService.incrementSkipProcessCount();
    }

    @Override
    public void onSkipInWrite(TransactionRecord item, Throwable t) {
        log.warn("WRITE ERROR");
        log.warn("SKIPPED IN WRITE - ID: {}", (item != null ? item.getTransactionId() : "UNKNOWN"));
        log.warn("REASON: {}", t.getMessage());

        metricsService.incrementSkipWriteCount();
    }
}