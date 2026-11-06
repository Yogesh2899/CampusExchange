package com.campusexchange.service;

import com.campusexchange.model.*;
import com.campusexchange.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
  private final UserRepository users;
  private final PasswordEncoder encoder;

  public AccountService(UserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  @Transactional
  public void updateProfile(Long id, String name, String phone, String department, String hostel) {
    if (phone == null || phone.isBlank()) {
      throw new IllegalArgumentException("Phone is required.");
    }
    if (hostel == null || hostel.isBlank()) {
      throw new IllegalArgumentException("Hostel is required.");
    }
    User user = users.findById(id).orElseThrow();
    user.updateProfile(
        name.trim(), phone.trim(), department == null ? "" : department.trim(), hostel.trim());
  }

  @Transactional
  public User register(String name, String email, String password) {
    if (!com.campusexchange.config.CollegeEmailPolicy.isAllowed(email)) {
      throw new IllegalArgumentException("Use your college email ending in @thapar.edu.");
    }
    if (password == null
        || password.length() < 8
        || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException(
          "Use a password of at least 8 characters and at most 72 UTF-8 bytes.");
    String normalized = com.campusexchange.config.CollegeEmailPolicy.normalize(email);
    if (users.existsByEmailIgnoreCase(normalized))
      throw new IllegalArgumentException("An account with that email already exists.");
    return users.save(new User(name.trim(), normalized, encoder.encode(password), Role.STUDENT));
  }
}
