package com.example.part35teammonew.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import com.example.part35teammonew.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserDto {

    private UUID id;

    private String email;

    private String nickname;

    private LocalDateTime createdAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .createdAt(user.getCreatedAt())
            .build();
    }
}