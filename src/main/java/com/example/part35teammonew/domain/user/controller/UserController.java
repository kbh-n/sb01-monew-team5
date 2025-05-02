package com.example.part35teammonew.domain.user.controller;

import com.example.part35teammonew.domain.user.dto.UserDto;
import com.example.part35teammonew.domain.user.dto.UserLoginRequest;
import com.example.part35teammonew.domain.user.dto.UserRegisterRequest;
import com.example.part35teammonew.domain.user.dto.UserUpdateRequest;
import com.example.part35teammonew.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping(consumes = "application/json", produces = "application/json")
    ResponseEntity<UserDto> registerUser(@RequestBody @Valid UserRegisterRequest request){
        UserDto createdUserDto = userService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdUserDto);
    }

    // 로그인
    @PostMapping("/login")
    ResponseEntity<UserDto> login(@RequestBody UserLoginRequest request){
        UserDto userDto = userService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Monew-Request-User-ID", userDto.getId().toString())
                .body(userDto);
    }

    // 닉네임 수정
    @PatchMapping("/{userId}")
    ResponseEntity<UserDto> updateUser(@PathVariable(value = "userId") UUID userId, @RequestBody UserUpdateRequest request) {
        UserDto updatedUserDto = userService.update(userId, request);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedUserDto);
    }

    // 회원 논리 삭제
    @DeleteMapping("/{userId}")
    ResponseEntity<Void> deleteUserLogical(@PathVariable(value = "userId") UUID userId){
        userService.deleteLogical(userId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }

    // 회원 물리 삭제
    @DeleteMapping("/{userId}/hard")
    ResponseEntity<Void> deleteUserPhysical(@PathVariable(value = "userId") UUID userId){
        userService.deletePhysical(userId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }
}
