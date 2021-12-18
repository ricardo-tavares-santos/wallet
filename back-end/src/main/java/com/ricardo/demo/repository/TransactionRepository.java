package com.ricardo.demo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ricardo.demo.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  List<Transaction> findByPlayerId(long playerId, Pageable pageable);
}