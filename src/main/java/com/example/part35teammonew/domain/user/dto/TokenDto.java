package com.example.part35teammonew.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@Builder
public class TokenDto {

  @JsonProperty("token")  // 직렬화 시 JSON에서 필드명을 token으로 설정
  private String token;
}