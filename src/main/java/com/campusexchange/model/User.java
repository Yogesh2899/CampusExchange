package com.campusexchange.model;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@org.hibernate.annotations.DynamicUpdate
@Table(name = "users")
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 160)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private Role role = Role.STUDENT;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(length = 30)
  private String phone = "";

  @Column(length = 100)
  private String department = "";

  @Column(length = 100)
  private String hostel = "";

  protected User() {}

  public User(String name, String email, String passwordHash, Role role) {
    this.name = name;
    this.email = email.toLowerCase();
    this.passwordHash = passwordHash;
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public Role getRole() {
    return role;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getPhone() {
    return phone;
  }

  public String getDepartment() {
    return department;
  }

  public String getHostel() {
    return hostel;
  }

  public void updateProfile(String name, String phone, String department, String hostel) {
    this.name = name;
    this.phone = phone;
    this.department = department;
    this.hostel = hostel;
  }
}
