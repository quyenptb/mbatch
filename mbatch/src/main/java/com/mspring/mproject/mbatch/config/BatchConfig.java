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
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;


import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private TransactionReader reader;

    @Autowired
    private TransactionProcessor processor;

    @Autowired
    private TransactionWriter writer;


    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("oracle.jdbc.OracleDriver")
                .url("jdbc:oracle:thin:@localhost:1521:xe")
                .username("your_username")
                .password("your_password")
                .build();
    }

    //Config TransactionManager
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    //Config JobRepository
    @Bean
    public JobRepository jobRepository(DataSource dataSource,
                                       PlatformTransactionManager txManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(txManager);
        factory.setDatabaseType("ORACLE");
        factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    //JobLauncher Spring Boot tự gán


    @Bean
    public JobExplorer jobExplorer(DataSource dataSource) throws Exception {
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        factory.afterPropertiesSet();
        return factory.getObject();
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
    public Step processTransactionStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("processOrdersStep", jobRepository)
                .<TransactionRecord, TransactionRecord>chunk(1000, txManager)
                .reader(reader.transactionReader())
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job processTransactionJob(JobRepository jobRepository, Step processTransactionStep) {
        return new JobBuilder("processTransactionJob", jobRepository).flow(processTransactionStep)
                .end()
                .build();
    }




}
