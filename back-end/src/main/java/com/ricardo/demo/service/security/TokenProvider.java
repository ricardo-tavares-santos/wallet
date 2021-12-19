package com.ricardo.demo.service.security;

import com.ricardo.demo.model.PlayerEntity;
import com.ricardo.demo.repository.PlayerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TokenProvider {
    
  private final String secretKey;

  private final long tokenValidityInMilliseconds;

  @Autowired
  PlayerRepository userRepository;

  public TokenProvider(AppConfig config, PlayerRepository userService) {
    this.secretKey = Base64.getEncoder().encodeToString(config.getSecret().getBytes());
     this.tokenValidityInMilliseconds = 1000 * config.getTokenValidityInSeconds();
  }

  public String createToken(PlayerEntity player) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + this.tokenValidityInMilliseconds);

    return Jwts.builder().setId(UUID.randomUUID().toString()).setSubject(player.getEmail())
            .claim("player",player)
        .setIssuedAt(now).signWith(SignatureAlgorithm.HS512, this.secretKey)
        .setExpiration(validity).compact();
  }

  public Authentication getAuthentication(String token) {
    String email = Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token)
        .getBody().getSubject();

    List<PlayerEntity> users = new ArrayList<PlayerEntity>();
    userRepository.findByEmail(email).forEach(users::add);
    PlayerEntity user = users.size()>0?users.get(0):null;

    if (user == null) {
      throw new UsernameNotFoundException("User '" + email + "' not found");
    }

    UserDetails userDetails = org.springframework.security.core.userdetails.User
        .withUsername(email).password("").authorities(Set.of()).accountExpired(false)
        .accountLocked(false).credentialsExpired(false).disabled(false).build();

    return new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());
  }
}
