package com.mspring.mproject.mbatch.repository;

import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionResitory extends JpaRepository<TransactionRecord, Long> {


}
