package com.example.part35teammonew.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateRequest {

    @NotBlank(message = "닉네임은 필수값입니다.")
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    String nickname;
}
