package com.mspring.mproject.mbatch.config;



import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

@Component
public class TransactionSkipListener implements SkipListener<TransactionRecord, TransactionRecord> {

    private static final Logger log = LoggerFactory.getLogger(TransactionSkipListener.class);
    private static final String ERROR_FILE = "transaction_errors.csv";

    //This method will run when error occurs in Processor
    @Override
    public void onSkipInProcess(TransactionRecord item, Throwable t) {
        log.warn("Skipped the Transaction ID: {} because of: {}", item.getTransactionId(), t.getMessage());

        try (FileWriter writer = new FileWriter(ERROR_FILE, true)) { //'true' flag for continue to write

            if (new java.io.File(ERROR_FILE).length() == 0) {
                writer.append("Transaction_ID,Error_Message\n");
            }

            writer.append(String.format("%s,%s\n",
                    item.getTransactionId(),
                    t.getMessage().replace(",", "") //delete "," in the error message, not to make CSV file broken
            ));
        } catch (IOException e) {
            log.error("Cannot write to the file: {}", e.getMessage());
        }
    }


    @Override
    public void onSkipInRead(Throwable t) {

    }

    @Override
    public void onSkipInWrite(TransactionRecord item, Throwable t) {

    }
}