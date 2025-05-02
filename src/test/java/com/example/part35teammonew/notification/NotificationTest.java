package com.example.part35teammonew.notification;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.part35teammonew.domain.notification.Enum.NotificationType;
import com.example.part35teammonew.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationTest {

  @Test
  @DisplayName("댓글  알림 생성 확인")
  void createCommentNoticeTest() {
    String content = "그르륷";
    UUID resourceId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Notification notification = Notification.createCommentNotice(userId, content, resourceId);

    assertThat(notification.getType()).isEqualTo(NotificationType.COMMENT);
    assertThat(notification.getContent()).isEqualTo(content);
    assertThat(notification.getResourceId()).isEqualTo(resourceId);
    assertThat(notification.isConfirmed()).isFalse();
    assertThat(notification.getUpdateAt()).isEqualTo(Instant.EPOCH);
    assertThat(notification.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
  }

  @Test
  @DisplayName("뉴스 알림 생성 확인")
  void createNewsNoticeTest() {
    String content = "크르릉컹";
    UUID resourceId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Notification notification = Notification.createNewsNotice(userId, content, resourceId);

    assertThat(notification.getType()).isEqualTo(NotificationType.NEWS);
    assertThat(notification.getContent()).isEqualTo(content);
    assertThat(notification.getResourceId()).isEqualTo(resourceId);
    assertThat(notification.isConfirmed()).isFalse();
    assertThat(notification.getUpdateAt()).isEqualTo(Instant.EPOCH);
    assertThat(notification.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
  }

  
}