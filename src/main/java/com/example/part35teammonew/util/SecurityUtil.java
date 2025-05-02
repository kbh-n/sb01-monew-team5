package com.example.part35teammonew.util;

import com.example.part35teammonew.domain.user.service.impl.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

// 서비스 계층에서 파라미터 없이도 현재 사용자의 userId를 'SecurityUtil.getCurrentUserId()'로 가져올 수 있음
public class SecurityUtil {

  private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

  private SecurityUtil() {}

  public static Optional<UUID> getCurrentUserId() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      logger.debug("Security Context에 인증 정보가 없습니다.");
      return Optional.empty();
    }

    UUID userId = null;
    if (authentication.getPrincipal() instanceof CustomUserDetails) {
      CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
      userId = customUserDetails.getUserId();
    }

    return Optional.ofNullable(userId);
  }
}