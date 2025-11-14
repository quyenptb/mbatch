package com.mspring.mproject.mbatch.batchstep.writer;
import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import com.mspring.mproject.mbatch.repository.TransactionRepository;

import lombok.AllArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

public class TransactionWriter implements ItemWriter<TransactionRecord> {


    @Autowired
    @Lazy
    private TransactionRepository transactionRepository;

    @Override
    public void write(Chunk<? extends TransactionRecord> items) throws Exception {
        transactionRepository.saveAll(items);
    }

}