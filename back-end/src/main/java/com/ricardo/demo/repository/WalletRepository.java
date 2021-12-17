package com.ricardo.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ricardo.demo.model.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
  Wallet findByPlayerId(long playerId);
}