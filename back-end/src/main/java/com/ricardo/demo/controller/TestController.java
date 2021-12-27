package com.ricardo.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class TestController {

  @GetMapping("/public")
  public String publicService() {
    return "This message is public";
  }

  @GetMapping("/secret")
  public String secretService(@AuthenticationPrincipal UserDetails details) {
    System.out.println(details.getUsername());
    return "A secret message";
  }

}