package com.mspring.mproject.mbatch.batchstep.writer;
import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import com.mspring.mproject.mbatch.repository.TransactionRepository;

import com.mspring.mproject.mbatch.config.BatchMetricsService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public class TransactionWriter implements ItemWriter<TransactionRecord> {


    @Autowired
    @Lazy
    private TransactionRepository transactionRepository;

    private final BatchMetricsService metricsService;

    public TransactionWriter(BatchMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public void write(Chunk<? extends TransactionRecord> items) throws Exception {
        transactionRepository.saveAll(items);

        metricsService.incrementWriteCount(items.size());
    }



}