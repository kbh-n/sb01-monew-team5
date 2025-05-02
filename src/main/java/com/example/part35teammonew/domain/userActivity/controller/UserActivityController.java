package com.example.part35teammonew.domain.userActivity.controller;

import com.example.part35teammonew.domain.userActivity.Dto.UserActivityDto;
import com.example.part35teammonew.domain.userActivity.service.UserActivityServiceInterface;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-activities")
public class UserActivityController {

  private final UserActivityServiceInterface userActivityServiceInterface;

  public UserActivityController(
      @Autowired UserActivityServiceInterface userActivityServiceInterface
  ) {
    this.userActivityServiceInterface = userActivityServiceInterface;
  }

  @GetMapping("{userId}")
  public ResponseEntity<UserActivityDto> getUserActivity(@PathVariable UUID userId) {
    UserActivityDto userActivityDto = userActivityServiceInterface.getUserActivity(userId);

    return ResponseEntity.ok(userActivityDto);
  }
}
