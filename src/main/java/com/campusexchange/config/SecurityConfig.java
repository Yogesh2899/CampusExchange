package com.campusexchange.config;

import com.campusexchange.repository.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(UserRepository users) {
    return email -> {
      if (!CollegeEmailPolicy.isAllowed(email)) {
        throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
            "Use a @thapar.edu college account");
      }
      return users
          .findByEmailIgnoreCase(CollegeEmailPolicy.normalize(email))
          .orElseThrow(
              () ->
                  new org.springframework.security.core.userdetails.UsernameNotFoundException(
                      "Account not found"));
    };
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository users)
      throws Exception {
    return http.authorizeHttpRequests(
            a ->
                a.requestMatchers("/admin", "/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        "/", "/items", "/register", "/login", "/css/**", "/js/**", "/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/items/*", "/photos/*")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterAfter(
            new AccountStatusFilter(users),
            org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class)
        .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
        .logout(l -> l.logoutSuccessUrl("/"))
        .build();
  }
}
