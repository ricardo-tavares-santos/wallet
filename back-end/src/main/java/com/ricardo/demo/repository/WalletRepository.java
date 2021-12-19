package com.ricardo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ricardo.demo.model.WalletEntity;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
  WalletEntity findByPlayerId(long playerId);
}