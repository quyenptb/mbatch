package com.mspring.mproject.mbatch.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job transactionJob;

    @Autowired
    public BatchController(JobLauncher jobLauncher, @Qualifier("transactionProcessingJob") Job transactionJob) {
        this.jobLauncher = jobLauncher;
        this.transactionJob = transactionJob;
    }

    @GetMapping("/start-process")
    public String startJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(transactionJob, jobParameters);

            return "Batch job đã được khởi động rồi";

        }
        catch (Exception e) {
            return "Lỗi xảy ra " + e.getMessage();

        }



    }




    

}
