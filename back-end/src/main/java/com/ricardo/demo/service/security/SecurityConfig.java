package com.ricardo.demo.service.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenProvider tokenProvider;

    public SecurityConfig(TokenProvider tokenProvider) {
      this.tokenProvider = tokenProvider;
    }
  
    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
      return authentication -> {
        throw new AuthenticationServiceException("Cannot authenticate " + authentication);
      };
    }
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {

      http
         // .cors(Customizer.withDefaults())
          .csrf(cust -> cust.disable())
          .sessionManagement(
              customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          // optional, if you want to access the
          // services from a browser
          // .httpBasic(Customizer.withDefaults())
          .authorizeRequests(customizer -> {
            customizer.antMatchers("/signup", "/login", "/public").permitAll(); //???
            customizer.anyRequest().authenticated();
          })
          .addFilterAfter(new JWTFilter(this.tokenProvider),
              SecurityContextPersistenceFilter.class)
          .cors()    
          ;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration corsConfiguration = new CorsConfiguration();
      //corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
      corsConfiguration.setAllowedHeaders(List.of("*"));
      corsConfiguration.setAllowedOriginPatterns(List.of("*"));
      //corsConfiguration.setAllowedOrigins(List.of("*"));
      corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PUT","OPTIONS","PATCH", "DELETE"));
      corsConfiguration.setAllowCredentials(true);
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", corsConfiguration);
      return source;
    }
    
}
