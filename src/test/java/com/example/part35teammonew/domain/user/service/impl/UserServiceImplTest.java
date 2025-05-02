package com.example.part35teammonew.domain.user.service.impl;

import com.example.part35teammonew.domain.user.dto.UserDto;
import com.example.part35teammonew.domain.user.dto.UserRegisterRequest;
import com.example.part35teammonew.domain.user.dto.UserUpdateRequest;
import com.example.part35teammonew.domain.user.entity.User;
import com.example.part35teammonew.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User user;
    private UserRegisterRequest registerRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("testuser")
                .password("Password123!")
                .build();

        registerRequest = UserRegisterRequest.builder()
                .email("test@example.com")
                .nickname("testuser")
                .password("Password123!")
                .build();

        updateRequest = UserUpdateRequest.builder()
                .nickname("updatedUser")
                .build();
    }

//    @Test
//    @DisplayName("회원가입 성공 테스트")
//    void registerUser_Success() {
//        // Given
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        // When
//        UserDto result = userService.register(registerRequest);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getEmail()).isEqualTo(registerRequest.getEmail());
//        assertThat(result.getNickname()).isEqualTo(registerRequest.getNickname());
//        verify(userRepository, times(1)).save(any(User.class));
//    }

    @Test
    @DisplayName("닉네임 수정 성공 테스트")
    void updateNickname_Success() {
        // Given
        User updatedUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("updatedUser")
                .password("Password123!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserDto result = userService.update(userId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(updateRequest.getNickname());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("닉네임 수정 실패 테스트 - 사용자 없음")
    void updateNickname_UserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> userService.update(userId, updateRequest));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("회원 논리 삭제 성공 테스트")
    void deleteUserLogical_Success() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.deleteLogical(userId);

        // Then
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("회원 논리 삭제 실패 테스트 - 사용자 없음")
    void deleteUserLogical_UserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> userService.deleteLogical(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("회원 물리 삭제 성공 테스트")
    void deleteUserPhysical_Success() {
        // Given
        doNothing().when(userRepository).deleteById(userId);

        // When
        userService.deletePhysical(userId);

        // Then
        verify(userRepository, times(1)).deleteById(userId);
    }
}