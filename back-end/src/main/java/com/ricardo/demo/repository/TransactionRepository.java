package com.ricardo.demo.repository;

import com.ricardo.demo.type.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ricardo.demo.model.TransactionEntity;
import org.springframework.transaction.annotation.Transactional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
  Page<TransactionEntity> findByPlayerId(long playerId, Pageable pageable);
  TransactionEntity findByTransactionIdAndTypeTransaction (String transactionId, TransactionType transactionType);
  @Transactional
  Long deleteAllByPlayerId(long playerId);
}