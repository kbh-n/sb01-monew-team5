package com.example.part35teammonew.domain.user.service.impl;


import com.example.part35teammonew.domain.user.entity.User;
import com.example.part35teammonew.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Component("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  // username은 그냥 '식별자'라는 의미. 꼭 이름이 아니어도 됨
  @Override
  @Transactional
  public UserDetails loadUserByUsername(final String email) {
    return userRepository.findByEmailAndIsDeletedFalse(email)
        .map(user -> createUser(email, user))
        .orElseThrow(() -> new UsernameNotFoundException(email + " -> 데이터베이스에서 찾을 수 없습니다."));
  }

  // CustomUserDetails를 이용해 userId 가져오기
  private CustomUserDetails createUser(String email, User user) {
    if (user.isDeleted()) {
      throw new RuntimeException(email + " -> 탈퇴한 사용자입니다.");
    }

    return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword());
  }
}