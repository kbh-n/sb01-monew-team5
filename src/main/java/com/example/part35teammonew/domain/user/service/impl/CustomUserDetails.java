package com.example.part35teammonew.domain.user.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {
  private UUID userId;
  private String username;
  private String password;

  public CustomUserDetails(UUID userId, String username, String password) {
    this.userId = userId;
    this.username = username;
    this.password = password;
  }

  public UUID getUserId() {
    return userId;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  // 권한 관련 메서드 (아직 권한을 설계하지 않았으므로 임의로 ROLE_USER 권한 부여)
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
  }

  // 계정이 만료되지 않았는지 여부
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  // 계정이 잠겨 있지 않은지 여부
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  // 자격 증명이 만료되지 않았는지 여부
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  // 계정이 활성화되었는지 여부
  @Override
  public boolean isEnabled() {
    return true;
  }
}
