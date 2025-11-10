package com.mspring.mproject.mbatch.batchstep.writer;
import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import com.mspring.mproject.mbatch.repository.TransactionResitory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import java.util.List;

public class TransactionWriter implements ItemWriter<TransactionRecord> {

    private final TransactionResitory repository;

    public TransactionWriter(TransactionResitory repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends TransactionRecord> items) throws Exception {
        repository.saveAll(items);
    }

}