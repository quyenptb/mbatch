package com.mspring.mproject.mbatch.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;


@Service
public class BatchMetricsService {

    private final Counter stepWriteCounter;
    private final Counter stepSkipProcessCounter;
    private final Counter stepSkipWriteCounter;
    private final Counter stepSkipReadCounter;


    private static final String METRIC_WRITE_TOTAL = "mbatch.step.write.total";
    private static final String METRIC_SKIP_PROCESS_TOTAL = "mbatch.step.skip.process.total";
    private static final String METRIC_SKIP_WRITE_TOTAL = "mbatch.step.skip.write.total";
    private static final String METRIC_SKIP_READ_TOTAL = "mbatch.step.skip.read.total";


    public BatchMetricsService(MeterRegistry registry) {
        this.stepWriteCounter = Counter.builder(METRIC_WRITE_TOTAL)
                .description("Total records successfully written")
                .register(registry);

        this.stepSkipProcessCounter = Counter.builder(METRIC_SKIP_PROCESS_TOTAL)
                .description("Total records skipped due to PROCESS error")
                .register(registry);

        this.stepSkipWriteCounter = Counter.builder(METRIC_SKIP_WRITE_TOTAL)
                .description("Total records skipped due to WRITE error")
                .register(registry);

        this.stepSkipReadCounter = Counter.builder(METRIC_SKIP_READ_TOTAL)
                .description("Total records skipped due to READ error")
                .register(registry);
    }


    public void incrementWriteCount(int count) {
        this.stepWriteCounter.increment(count);
    }

    public void incrementSkipProcessCount() {
        this.stepSkipProcessCounter.increment();
    }

    public void incrementSkipWriteCount() {
        this.stepSkipWriteCounter.increment();
    }

    public void incrementSkipReadCount() {
        this.stepSkipReadCounter.increment();
    }


    public double getStepWriteCount() {
        return this.stepWriteCounter.count();
    }

    public double getStepSkipProcessCount() {
        return this.stepSkipProcessCounter.count();
    }

    public double getStepSkipWriteCount() {
        return this.stepSkipWriteCounter.count();
    }

    public double getStepSkipReadCount() {
        return this.stepSkipReadCounter.count();
    }


    public void resetAllCounters() {
        this.stepWriteCounter.increment(-this.stepWriteCounter.count());
        this.stepSkipProcessCounter.increment(-this.stepSkipProcessCounter.count());
        this.stepSkipWriteCounter.increment(-this.stepSkipWriteCounter.count());
        this.stepSkipReadCounter.increment(-this.stepSkipReadCounter.count());
    }
}