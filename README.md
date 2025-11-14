# EOD Banking Transaction Processing  
[![Spring Batch](https://img.shields.io/badge/Spring%20Batch-5.0-brightgreen)]()  
[![Java](https://img.shields.io/badge/Java-21-blue)]()  
[![Oracle](https://img.shields.io/badge/Oracle-21c-red)]()

A robust End-of-Day (EOD) transaction processing system built with **Spring Boot**, **Spring Batch**, and a full observability pipeline using **Micrometer → Prometheus → Grafana**.  
The system handles large-volume banking transactions, applying cleansing, validation, fault tolerance, and real-time monitoring.

---

## About EOD 
End-of-Day (EOD) processing is a vital operation within the banking sector, designed to accurately record, validate, and securely store all daily transactions. This comprehensive procedure is essential for maintaining data integrity, facilitating financial reporting, and ensuring adherence to stringent regulatory compliance standards.

## Monitoring & Observability  
> **Insight at scale:** A complete metrics pipeline that tracks system health, throughput, and failures.

### Key Monitoring Views  
- **Job Health:** Duration, execution counts, success/failure rates  
- **Throughput:** Records read vs. written  
- **Error Audits:** Processor skips vs. writer skips  

Dashboards are powered by a **weeklong EOD simulation** with varied data patterns.

---

## Core Features  

| Feature | Description | Example / Notes |
|---------|-------------|----------------|
| **Dynamic Job Parameters** | Job parameters (like `processingDate`) are injected via `@JobScope` and `@StepScope`. | ```java @Value("#{jobParameters['processingDate']}") private LocalDate processingDate; ``` |
| **Reader Data Cleansing** | Trims text fields and normalizes financial values. | `BigDecimal.setScale(2, RoundingMode.HALF_UP)` |
| **Processor Business Validation** | Rejects mismatched dates and applies banking validation rules. Invalid records are skipped. | Returns `null` for invalid records |
| **Fault Tolerance Mechanism** | Handles processor-level business errors and writer-level database exceptions without stopping the job. | Configurable skip logic |
| **Skip Auditing** | Tracks all skipped records via `TransactionSkipListener` and pushes metrics to Prometheus/Grafana. | Enables full observability of failures |
| **Automated Scheduling** | Runs EOD batch jobs automatically via a cron trigger. | Supports manual execution through REST endpoints |

## Architecture Overview
### Processing Flow


Component Summary
Component	Responsibility
Reader	Load 10k+ CSV records + cleanse data
Processor	Validate business rules + apply processing date
Writer	Persist valid entries to Oracle via saveAll
Controller/Scheduler	Manual or cron-based job execution
Metrics Pipeline	Spring Batch → Micrometer → Prometheus → Grafana

## Technology Stack
[![Java 21](https://img.shields.io/badge/Java-21-blue)]()
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)]()
[![Spring Batch 5](https://img.shields.io/badge/Spring%20Batch-5-lightgrey)]()
[![Oracle 21c](https://img.shields.io/badge/Oracle-21c-red)]()
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-2F74C0)]()
[![Micrometer](https://img.shields.io/badge/Micrometer-00BFFF)]()
[![Prometheus](https://img.shields.io/badge/Prometheus-FF6600)]()
[![Grafana](https://img.shields.io/badge/Grafana-F46800)]()
[![Maven](https://img.shields.io/badge/Maven-C71A36)]()
[![Lombok](https://img.shields.io/badge/Lombok-FF0000)]()

## Setup Guide
Database Configuration
Start an Oracle 21c instance

Update spring.datasource.* in application.properties

Hibernate autogenerates TransactionRecord

Batch metadata tables come from schema-oracle.sql

Running the Application
Start:
```bash
mvn spring-boot:run
```
Then access:

```
http://localhost:8080
```
Deploying Monitoring Stack
Prometheus scrapes metrics at /actuator/prometheus

Grafana dashboards are powered by provided PromQL queries

Generating Dashboard Data
Uncomment the @Scheduled(...) annotation in JobScheduler.java

Run with different processingDate values to simulate:

- Clean data days

- Dirty data days

- Empty input days

### Grafana Dashboard

![EOD Total Job Runs](https://drive.google.com/uc?export=view&id=1GHWACXE0r_B2uHT1wf-IJkdgDcqddHoc)

![Longest Step Runtime](https://drive.google.com/uc?export=view&id=1-E5XZgO5jhoa-XLJKaBCp8-riMTgfyiD)

![Total Job Runtime Overview](https://drive.google.com/uc?export=view&id=1IsINIOQ8KebFhNtieaO2hxnCIQbWPS9O)

![Item Read Processing Time](https://drive.google.com/uc?export=view&id=17hOmZouPdPef9bQkbkIDYN_LobI_Yd5X)






