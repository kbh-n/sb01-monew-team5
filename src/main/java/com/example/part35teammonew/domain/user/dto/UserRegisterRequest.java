package com.example.part35teammonew.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRegisterRequest {

    @NotBlank(message = "이메일은 필수값입니다.")
    @Email(message = "이메일 형식이 아닙니다.")
    String email;

    @NotBlank(message = "닉네임은 필수값입니다.")
    @Size(min = 2, max = 16, message = "닉네임은 2자 이상 16자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,16}$", message = "닉네임은 2~16자의 한글, 영어 또는 숫자만 사용 가능합니다.")
    String nickname;

    @NotBlank(message = "비밀번호는 필수값입니다.")
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    String password;
}
