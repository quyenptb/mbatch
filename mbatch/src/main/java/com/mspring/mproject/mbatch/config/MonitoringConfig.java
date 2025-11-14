package com.mspring.mproject.mbatch.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.JobExecution;

@Configuration
@EnableBatchProcessing
public class MonitoringConfig {

    @Bean
    public JobExecutionListener batchJobMetrics(MeterRegistry meterRegistry) {
        return new JobExecutionListenerSupport() {
            @Override
            public void afterJob(JobExecution jobExecution) {
                meterRegistry.counter("batch.jobs.completed", "job", jobExecution.getJobInstance().getJobName())
                        .increment();
            }
        };
    }
}
