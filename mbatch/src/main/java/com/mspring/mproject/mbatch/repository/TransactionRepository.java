package com.mspring.mproject.mbatch.repository;

import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {


    @Query("SELECT " +
            "   COUNT(t), " +
            "   SUM(t.transactionAmount), " +
            "   COUNT(DISTINCT t.customerId) " +
            "FROM TransactionRecord t " +
            "WHERE t.transactionDate = :processingDate")
    List<Object[]> getReconciliationData(@Param("processingDate") LocalDate processingDate);
}
