package com.mspring.mproject.mbatch.batchstep.processor;

import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionProcessor implements ItemProcessor<TransactionRecord, TransactionRecord> {

    @Override
    public TransactionRecord process(TransactionRecord item) throws Exception {
        if (item.getUpdatedBalance().compareTo(BigDecimal.ZERO) < 0) {
            return null;
            //throw new IllegalArgumentException("Updated balance cannot be negative for Transaction ID: " + item.getTransactionId());
        }
        if (item.getTransactionAmount().compareTo(item.getTotalBalance().multiply(new BigDecimal("0.5"))) > 0) {
            item.setIsAnomaly(true);
        }
        return item;
    }
}
