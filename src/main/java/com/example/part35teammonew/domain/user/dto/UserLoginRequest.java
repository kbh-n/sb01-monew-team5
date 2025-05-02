package com.example.part35teammonew.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginRequest {

  @NotBlank(message = "이메일은 필수값입니다.")
  @Email(message = "이메일 형식이 아닙니다.")
  String email;

  @NotBlank(message = "비밀번호는 필수값입니다.")
  @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
  String password;

}
