package com.ricardo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class RicardoDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RicardoDemoApplication.class, args);
	}

	// encrypt passwords
	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}	

}
