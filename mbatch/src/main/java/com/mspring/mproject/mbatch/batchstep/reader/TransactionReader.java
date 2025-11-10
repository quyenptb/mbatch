package com.mspring.mproject.mbatch.batchstep.reader;

import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Configuration
public class TransactionReader {

    @Bean
    public FlatFileItemReader<TransactionRecord> transactionReader() {
        FlatFileItemReader<TransactionRecord> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("excel_transaction_data.csv"));
        reader.setLinesToSkip(1); // bỏ header

        // Tokenizer: định nghĩa tên cột trong CSV
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames(
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
                "Processed_At"
        );
        lineTokenizer.setStrict(false);

        DateTimeFormatter transactionFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter processedFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        // Mapper: ánh xạ dòng CSV → TransactionRecord
        FieldSetMapper<TransactionRecord> fieldSetMapper = fieldSet -> {
            TransactionRecord tr = new TransactionRecord();
            tr.setBatchId(fieldSet.readString("Batch_ID"));
            tr.setTransactionId(fieldSet.readLong("Transaction_ID"));
            tr.setCustomerId(fieldSet.readLong("Customer_ID"));
            tr.setAccountType(fieldSet.readString("Account_Type"));
            tr.setTotalBalance(fieldSet.readBigDecimal("Total_Balance"));
            tr.setTransactionAmount(fieldSet.readBigDecimal("Transaction_Amount"));
            tr.setUpdatedBalance(fieldSet.readBigDecimal("Updated_Balance"));
            tr.setInvestmentAmount(fieldSet.readBigDecimal("Investment_Amount"));
            tr.setInvestmentType(fieldSet.readString("Investment_Type"));
            tr.setIsAnomaly(fieldSet.readInt("Is_Anomaly") == 1);
            tr.setTransactionDate(LocalDate.parse(fieldSet.readString("Transaction_Date"), transactionFormatter));
            tr.setProcessedAt(LocalDateTime.parse(fieldSet.readString("Processed_At"), processedFormatter));
            tr.setYear(fieldSet.readInt("Year"));
            tr.setMonth(fieldSet.readInt("Month"));
            tr.setDay(fieldSet.readInt("Day"));
            return tr;
        };

        // LineMapper
        DefaultLineMapper<TransactionRecord> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);

        return reader;
    }
}
