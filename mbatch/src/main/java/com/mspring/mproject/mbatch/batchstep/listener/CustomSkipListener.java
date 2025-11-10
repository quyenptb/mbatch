package com.mspring.mproject.mbatch.batchstep.listener;

import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import org.hibernate.sql.ast.spi.SqlAliasStemHelper;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomSkipListener implements SkipListener<TransactionRecord, TransactionRecord> {

    @Autowired
    private FlatFileItemWriter<TransactionRecord> failedTransactionWriter;

    @Override
    public void onSkipInRead(Throwable t) {
        System.out.println("Error when read transaction:" + t.getMessage());
    }

    @Override
    public void onSkipInProcess(TransactionRecord item, Throwable t) {
        System.out.println("Error when process this item: " + item + "- Erorr" + t);

            try {
                failedTransactionWriter.write(Chunk.of(item));
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    @Override
    public void onSkipInWrite(TransactionRecord item, Throwable t) {
        System.out.println("Error occurs when write item: " + item + " error: t") ;

        try {
            failedTransactionWriter.write(Chunk.of(item));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
