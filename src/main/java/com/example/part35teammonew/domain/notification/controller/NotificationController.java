package com.example.part35teammonew.domain.notification.controller;

import com.example.part35teammonew.domain.notification.Dto.CursorPageRequest;
import com.example.part35teammonew.domain.notification.Dto.CursorPageResponse;
import com.example.part35teammonew.domain.notification.Dto.NotificationDto;
import com.example.part35teammonew.domain.notification.service.NotificationServiceInterface;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationServiceInterface notificationServiceInterface;

  public NotificationController(
      @Autowired NotificationServiceInterface notificationServiceInterface) {
    this.notificationServiceInterface = notificationServiceInterface;
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<NotificationDto>> getNotifications(
      @RequestParam(value = "cursor", required = false) Instant  cursor,
      @RequestParam(value = "after", required = false) Instant after,
      @RequestParam(value = "limit") int limit,
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId
  ) {
    CursorPageRequest cursorPageRequest = new CursorPageRequest(cursor, after, limit);
    CursorPageResponse<NotificationDto> result =
        notificationServiceInterface.getNoticePage(requestUserId, cursorPageRequest);

    return ResponseEntity.ok(result);
  }

  @PatchMapping
  public ResponseEntity<String> getUserNotificationAllRead(
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId
  ) {
    try {

      boolean success = notificationServiceInterface.confirmedReadAllNotice(requestUserId);

      if (success) {
        return ResponseEntity.ok("알림 확인 성공");
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 정보 없음 또는 알림 없음");
      }
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body("잘못된 요청 (입력값 검증 실패)");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류");
    }
  }


  @PatchMapping("/{notificationId}")
  public ResponseEntity<String> getNotificationRead(
      @PathVariable("notificationId") ObjectId notificationId,
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId
  ) {
    try {
      boolean success = notificationServiceInterface.confirmedReadNotice(notificationId,
          requestUserId);

      if (success) {
        return ResponseEntity.ok("알림 확인 성공");
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 정보 없음 또는 알림 없음");
      }
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body("잘못된 요청 (입력값 검증 실패)");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류");
    }
  }

}