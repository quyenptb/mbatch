package com.mspring.mproject.mbatch.batchstep.writer;

import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import com.mspring.mproject.mbatch.repository.TransactionRespoitory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class FailedRecordWriterConfig {
    @Bean
    public FlatFileItemWriter<TransactionRecord> failedRecordsWriter() {
        FlatFileItemWriter<TransactionRecord> writer = new FlatFileItemWriter<>();

        writer.setResource(new FileSystemResource("target/failed_records.csv"));
        writer.setAppendAllowed(true);

        DelimitedLineAggregator<TransactionRecord> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<TransactionRecord> fieldExtractor = new BeanWrapperFieldExtractor<>();

        fieldExtractor.setNames(new String[]{
                "Batch_ID",
                "Transaction_ID",
                "Customer_ID",
                "Account_Type",
                "Total_Balance",
                "Transaction_Amount",
                "Updated_Balance",
                "Investment_Amount",
                "Investment_Type",
                "Is_Anomaly",
                "Transaction_Date",
                "Year",
                "Month",
                "Day",
                "Processed_At" }
        );
        lineAggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(lineAggregator);

        return writer;


    }
}
