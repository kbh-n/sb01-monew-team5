package com.example.part35teammonew.domain.user.service;

import com.example.part35teammonew.domain.user.dto.UserDto;
import com.example.part35teammonew.domain.user.dto.UserLoginRequest;
import com.example.part35teammonew.domain.user.dto.UserRegisterRequest;
import com.example.part35teammonew.domain.user.dto.UserUpdateRequest;
import java.util.UUID;

public interface UserService {

    // 회원가입
    UserDto register(UserRegisterRequest request);

    // 로그인
    UserDto login(UserLoginRequest request);

    // 닉네임 수정
    UserDto update(UUID userId, UserUpdateRequest request);

    // 회원 논리 삭제
    void deleteLogical(UUID userId);

    // 회원 물리 삭제
    void deletePhysical(UUID userId);
}
