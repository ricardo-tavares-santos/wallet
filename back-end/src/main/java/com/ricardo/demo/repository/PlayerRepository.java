package com.ricardo.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ricardo.demo.model.PlayerEntity;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
  List<PlayerEntity> findByEmail(String email);
}