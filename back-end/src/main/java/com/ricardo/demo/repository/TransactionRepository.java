package com.ricardo.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ricardo.demo.model.TransactionEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
  Page<TransactionEntity> findByPlayerId(long playerId, Pageable pageable);
}