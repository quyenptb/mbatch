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


import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
//@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private TransactionProcessor processor;

    @Autowired
    private TransactionWriter writer;

    @Autowired
    private TransactionSkipListener transactionSkipListener;

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

    @Bean(name = "transactionItemReader")
    @StepScope //StepScope has to be with the Bean
    public FlatFileItemReader<TransactionRecord> transactionReader(
            @Value("#{jobParameters['inputFile']}")
            String inputFile,
            @Value("#{jobParameters['processingDate']}")
            String processingDate
    ) {

        //Transfer String (processingDate parameter above) -> LocalDate (expectedDate)
        LocalDate expectedDate = LocalDate.parse(processingDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        FlatFileItemReader<TransactionRecord> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(inputFile));
        reader.setLinesToSkip(1);

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

        FieldSetMapper<TransactionRecord> fieldSetMapper = fieldSet -> {

            LocalDate transactionDate = LocalDate.parse(fieldSet.readString("Transaction_Date"), transactionFormatter);

            if (!transactionDate.equals(expectedDate)) {
                return null;
            }

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
            tr.setTransactionDate(transactionDate);
            tr.setProcessedAt(LocalDateTime.parse(fieldSet.readString("Processed_At"), processedFormatter));
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
                                       FlatFileItemReader<TransactionRecord> transactionReader
                                       ) {
        return new StepBuilder("processOrdersStep", jobRepository)
                .<TransactionRecord, TransactionRecord>chunk(1000, txManager)
                .reader(transactionReader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor())
                //Fault Tolerant Mode
                .faultTolerant()
                .skipLimit(200) //bo toi da 200 loi
                .skip(Exception.class) //ngoai tru Exception thi khong ghi lai
                .listener(transactionSkipListener)
                .build();
    }

    @Bean(name = "transactionProcessingJob")
    public Job processTransactionJob(JobRepository jobRepository, Step processTransactionStep) {
        return new JobBuilder("processTransactionJob", jobRepository).flow(processTransactionStep)
                .end()
                .build();
    }


    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        // Sử dụng script chuẩn của Spring Batch cho Oracle
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-oracle.sql"));

        populator.setContinueOnError(true); //phòng khi bảng đã tồn tại

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }




}
