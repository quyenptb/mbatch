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
                    .addLong("run.id", System.currentTimeMillis())
                    .addString("processingDate", "2022-09-09") //LocalDate.now().toString()
                    .addString("inputFile", "transaction_data.csv" )
                    //.addString("inputFile", "transaction_data.csv")
                    .toJobParameters();

            jobLauncher.run(transactionJob, jobParameters);

            return "Done batch job processing";

        }
        catch (Exception e) {
            return "An error has occur " + e.getMessage();

        }

    }




    

}
