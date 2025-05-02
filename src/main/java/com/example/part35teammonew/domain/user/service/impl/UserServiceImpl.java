package com.example.part35teammonew.domain.user.service.impl;

import com.example.part35teammonew.domain.user.dto.UserDto;
import com.example.part35teammonew.domain.user.dto.UserLoginRequest;
import com.example.part35teammonew.domain.user.dto.UserRegisterRequest;
import com.example.part35teammonew.domain.user.dto.UserUpdateRequest;
import com.example.part35teammonew.domain.user.entity.User;
import com.example.part35teammonew.domain.user.repository.UserRepository;
import com.example.part35teammonew.domain.user.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.example.part35teammonew.domain.userActivity.Dto.UserInfoDto;
import com.example.part35teammonew.domain.userActivity.service.UserActivityServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

// TODO 로그, 커스텀 예외 처리
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final UserActivityServiceInterface userActivityServiceInterface;


    // 회원가입
    @Override
    public UserDto register(UserRegisterRequest request) {
        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
            .email(request.getEmail())
            .nickname(request.getNickname())
            .password(encryptedPassword)
            .build();
        User savedUser = userRepository.save(user); // repository save된 버전 = DB에 저장된 버전을 return해야 반환값에 id랑 createdAt이 채워진다
        userActivityServiceInterface.createUserActivity(savedUser.getCreatedAt(),savedUser.getId(),savedUser.getNickname(),savedUser.getEmail());// 유저 생성에 맞춰 유저 활동내역 생성

        return UserDto.fromEntity(savedUser);
    }
  

    // 로그인
    @Override
    @Transactional(readOnly = true)
    public UserDto login(UserLoginRequest request){
        // 이메일로 사용자 검색 (논리 삭제가 되지 않은 사용자여야 함)
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(); // TODO 커스텀 예외 추가 후 수정 예정

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다."); // TODO 커스텀 예외 추가 후 수정 예정
        }
        return UserDto.fromEntity(user);
    }

    // 닉네임 수정
    @Transactional
    @Override
    public UserDto update(UUID userId, UserUpdateRequest request){
        User user = userRepository.findById(userId)
            .orElseThrow(); // TODO 커스텀 예외 추가 후 수정 예정
        user.updateNickname(request.getNickname());

        userActivityServiceInterface.updateUserInformation(userId,new UserInfoDto(request.getNickname()));

        return UserDto.fromEntity(user);
    }

    // 회원 논리 삭제
    @Transactional
    @Override
    public void deleteLogical(UUID userId){
        User user = userRepository.findById(userId)
            .orElseThrow();
        user.deleteLogical();
        userRepository.save(user);
    }

    // 회원 물리 삭제 - TODO 수정 필요 (Cascade)
    @Transactional
    @Override
    public void deletePhysical(UUID userId){
        
        userRepository.deleteById(userId);
        userActivityServiceInterface.deleteUserActivity(userId);//삭제할떄 물리적 삭제
    }
}
