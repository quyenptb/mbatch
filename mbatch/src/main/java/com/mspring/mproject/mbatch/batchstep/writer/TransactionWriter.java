package com.mspring.mproject.mbatch.batchstep.writer;
import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import com.mspring.mproject.mbatch.repository.TransactionRespoitory;
import com.mspring.mproject.mbatch.repository.TransactionRespoitory;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component

public class TransactionWriter implements ItemWriter<TransactionRecord> {

    private final TransactionRespoitory repository;

    public TransactionWriter(TransactionRespoitory repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends TransactionRecord> items) throws Exception {
        repository.saveAll(items);
    }

}