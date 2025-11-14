package com.mspring.mproject.mbatch.config;

import com.mspring.mproject.mbatch.batchstep.processor.TransactionProcessor;
import com.mspring.mproject.mbatch.batchstep.writer.TransactionWriter;
import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
// import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

// import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;


import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class BatchConfig {

    @Autowired
    private TransactionSkipListener transactionSkipListener;

    @Autowired
    private ReconciliationTasklet reconciliationTasklet;

    @Autowired
    private BatchMetricsService metricsService;

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

        /*
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("oracle.jdbc.OracleDriver")
                .url("jdbc:oracle:thin:@localhost:1521/orcl21pdb1")
                .username("C##mbatch_user")
                .password("123")
                .build();
    }

    //Config TransactionManager
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

     */

    /*
    //Config JobRepository
    @Bean
    public JobRepository jobRepository(DataSource dataSource,
                                       PlatformTransactionManager txManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(txManager);
        factory.setDatabaseType("ORACLE");
        factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
        //oracle does not accept too long table name, so i use T_BATCH for all table prefix
        //factory.setTablePrefix("B_");
        factory.afterPropertiesSet();
        return factory.getObject();
    } */

    //JobLauncher Spring Boot tự gán

    /*
    @Bean
    public JobExplorer jobExplorer(DataSource dataSource, PlatformTransactionManager txManager) throws Exception {
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(txManager);
        factory.afterPropertiesSet();
        return factory.getObject();
    } */


    @Bean
    @StepScope
    public TransactionProcessor transactionProcessor(@Value("#{jobParameters['processingDate']}") String processingDate) {
        return new TransactionProcessor(processingDate);
    }

    @Bean
    public TransactionWriter transactionWriter() {
        return new TransactionWriter(metricsService);
    }


    @Bean(name = "transactionItemReader")
    @StepScope
    public FlatFileItemReader<TransactionRecord> transactionReader(@Value("#{jobParameters['inputFile']}") String inputFile)
    {
        FlatFileItemReader<TransactionRecord> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(inputFile));
        reader.setLinesToSkip(1);

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames(
                "Batch_ID", "Transaction_ID", "Customer_ID", "Account_Type", "Total_Balance",
                "Transaction_Amount", "Updated_Balance", "Investment_Amount", "Investment_Type",
                "Is_Anomaly", "Transaction_Date", "Year", "Month", "Day", "Processed_At"
        );
        lineTokenizer.setStrict(false);

        DateTimeFormatter transactionFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter processedFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        FieldSetMapper<TransactionRecord> fieldSetMapper = fieldSet -> {

            String dateString = fieldSet.readString("Transaction_Date").trim();
            LocalDate transactionDate = LocalDate.parse(dateString, transactionFormatter);


            TransactionRecord tr = new TransactionRecord();
            tr.setBatchId(fieldSet.readString("Batch_ID"));
            tr.setTransactionId(fieldSet.readLong("Transaction_ID"));
            tr.setCustomerId(fieldSet.readLong("Customer_ID"));
            tr.setAccountType(fieldSet.readString("Account_Type"));
            tr.setTotalBalance(fieldSet.readBigDecimal("Total_Balance").setScale(2, RoundingMode.HALF_UP));
            tr.setTransactionAmount(fieldSet.readBigDecimal("Transaction_Amount").setScale(2, RoundingMode.HALF_UP));
            tr.setUpdatedBalance(fieldSet.readBigDecimal("Updated_Balance").setScale(2, RoundingMode.HALF_UP));
            tr.setInvestmentAmount(fieldSet.readBigDecimal("Investment_Amount").setScale(2, RoundingMode.HALF_UP));
            tr.setInvestmentType(fieldSet.readString("Investment_Type"));
            tr.setIsAnomaly(fieldSet.readInt("Is_Anomaly") == 1);
            tr.setTransactionDate(transactionDate);
            tr.setProcessedAt(LocalDateTime.parse(fieldSet.readString("Processed_At").trim(), processedFormatter));
            tr.setYear(fieldSet.readInt("Year"));
            tr.setMonth(fieldSet.readInt("Month"));
            tr.setDay(fieldSet.readInt("Day"));
            return tr;
        };

        DefaultLineMapper<TransactionRecord> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Step processTransactionStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                       FlatFileItemReader<TransactionRecord> transactionReader,
                                       TransactionProcessor transactionProcessor,
                                       TransactionWriter transactionWriter
    ) {
        return new StepBuilder("processOrdersStep", jobRepository)
                .<TransactionRecord, TransactionRecord>chunk(1000, txManager)
                .reader(transactionReader)
                .processor(transactionProcessor)
                .writer(transactionWriter)
                //.taskExecutor(taskExecutor())
                .faultTolerant()
                .skipLimit(200)
                .skip(Exception.class)
                .listener(transactionSkipListener)
                .build();
    }
    @Bean
    public Step reconciliationStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("reconciliationStep", jobRepository)
                .tasklet(reconciliationTasklet, txManager)
                .build();
    }

    @Bean(name = "transactionProcessingJob")
    public Job processTransactionJob(JobRepository jobRepository, Step processTransactionStep, Step reconciliationStep) {
        return new JobBuilder("processTransactionJob", jobRepository).start(processTransactionStep)
                .next(reconciliationStep)
                .build();
    }


    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-oracle.sql"));
        populator.setContinueOnError(true);
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}