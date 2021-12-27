package com.ricardo.demo.repository;

import com.ricardo.demo.model.BetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface BetRepository extends JpaRepository<BetEntity, Long> {
  BetEntity findByTransactionId(String transactionId);
  @Transactional
  Long deleteAllByPlayerId(long playerId);
}