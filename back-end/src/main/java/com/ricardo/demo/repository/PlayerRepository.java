package com.ricardo.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ricardo.demo.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
  List<Player> findByEmail(String email);
}