package com.mspring.mproject.mbatch.config;

import com.mspring.mproject.mbatch.batchstep.processor.TransactionProcessor;
import com.mspring.mproject.mbatch.batchstep.reader.TransactionReader;
import com.mspring.mproject.mbatch.batchstep.writer.TransactionWriter;
import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import com.mspring.mproject.mbatch.repository.TransactionRespoitory;
import com.mspring.mproject.mbatch.repository.TransactionRespoitory;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;


import javax.sql.DataSource;

@Configuration
//@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private TransactionReader reader;

    @Autowired
    private TransactionProcessor processor;

    @Autowired
    private TransactionWriter writer;

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
    public Step processTransactionStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("processOrdersStep", jobRepository)
                .<TransactionRecord, TransactionRecord>chunk(1000, txManager)
                .reader(reader.transactionReader())
                .processor(processor)
                .writer(writer)
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
