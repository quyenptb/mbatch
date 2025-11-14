package com.mspring.mproject.mbatch.config;


import com.mspring.mproject.mbatch.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@StepScope
public class ReconciliationTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationTasklet.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BatchMetricsService metricsService;

    private final LocalDate processingDate;

    @Autowired
    public ReconciliationTasklet(@Value("#{jobParameters['processingDate']}") String processingDateStr) {
        this.processingDate = LocalDate.parse(processingDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        StepExecution jobStepExecution = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getStepExecutions()
                .stream()
                .filter(step -> step.getStepName().equals("processOrdersStep"))
                .findFirst()
                .orElse(null);

        long readCount = 0;
        long filterCount = 0; //when processor return null

        if (jobStepExecution != null) {
            readCount = jobStepExecution.getReadCount();
            filterCount = jobStepExecution.getFilterCount();
        }

        long writeCount = (long) metricsService.getStepWriteCount();
        long skipProcessCount = (long) metricsService.getStepSkipProcessCount();
        long skipWriteCount = (long) metricsService.getStepSkipWriteCount();

        List<Object[]> results = transactionRepository.getReconciliationData(processingDate);

        long dbWriteCount = 0;
        BigDecimal dbTotalValue = BigDecimal.ZERO;
        long dbUniqueCustomers = 0;

        if (results != null && !results.isEmpty() && results.get(0)[0] != null) {
            Object[] result = results.get(0);
            dbWriteCount = ((Number) result[0]).longValue();
            dbTotalValue = (BigDecimal) result[1];
            dbUniqueCustomers = ((Number) result[2]).longValue();
        }


        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

        log.info("--- EOD RECONCILIATION REPORT (Date: {}) ---", processingDate);
        log.info("1. DATA PIPELINE METRICS:");
        log.info("   - Records Read (from CSV): {}", readCount);
        log.info("   - Records Filtered (by Date): {}", filterCount);
        log.info("   - Records Processed: {}", readCount - filterCount);
        log.info("   ----------------------------------------------");
        log.info("   - Records Written to DB: {}", writeCount);
        log.info("   - Records Skipped (Process Errors): {}", skipProcessCount);
        log.info("   - Records Skipped (Write Errors): {}", skipWriteCount);
        log.info("");
        log.info("2. BUSINESS AUDIT METRICS from Database:");
        log.info("   - DB Write Count Check: {}", dbWriteCount);
        log.info("   - Total Value Written to DB: {}", currencyFormatter.format(dbTotalValue));
        log.info("   - Unique Customers Affected: {}", dbUniqueCustomers);
        log.info("--- JOB COMPLETED SUCCESSFULLY ---");

        metricsService.resetAllCounters();

        return RepeatStatus.FINISHED;
    }
}