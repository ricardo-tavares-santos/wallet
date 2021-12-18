package com.ricardo.demo.controller;

import com.ricardo.demo.dto.TransactionDto;
import com.ricardo.demo.dto.test.IdempotencyDto;
import com.ricardo.demo.service.idempotency.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class TestController {

  @Autowired
  private TokenService tokenService;

  @GetMapping("/public")
  public String publicService() {
    return "This message is public";
  }

  @GetMapping("/secret")
  public String secretService(@AuthenticationPrincipal UserDetails details) {
    System.out.println(details.getUsername());
    return "A secret message";
  }

  @PostMapping("/idempotency")
  public ResponseEntity<IdempotencyDto> checkIdempotency(@RequestBody TransactionDto lTransactionDto) {
    IdempotencyDto lIdempotencyDto = new IdempotencyDto();
    String tokenMsg = tokenService.checkToken(lTransactionDto.getIdempotency_Key());
    lIdempotencyDto.setMsg(tokenMsg);
    if(tokenMsg=="OK") {
      return new ResponseEntity<>(lIdempotencyDto, HttpStatus.OK);
    }
    return new ResponseEntity<>(lIdempotencyDto, HttpStatus.CONFLICT);
  }

}