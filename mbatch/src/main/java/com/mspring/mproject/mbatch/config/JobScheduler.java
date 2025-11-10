package com.mspring.mproject.mbatch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class JobScheduler {
    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("transactionProcessingJob")
    private Job transactionJob;

    //test
    @Scheduled(fixedRate = 30000) // 30.000 mili-seconds = 30 seconds
    public void runJobForTest() {
        log.info("Scheduler: Triggering this job (test 30 s)...");
        runJob();
    }

/*
    @Scheduled(cron = "0 0 23 * * ?") //0 s, 0 m, 23 h everyday, every month
    public void runJobForProduction() {
            log.info("Scheduler: Running job at 23:00");
            runJob();
    } */

    private void runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("processingDate", LocalDateTime.now().toString())
                    .toJobParameters();

            jobLauncher.run(transactionJob, jobParameters);

        } catch (Exception e) {
            log.error("Scheduler: Triggering job FAILED. Error: {}", e.getMessage());
        }
    }
}
