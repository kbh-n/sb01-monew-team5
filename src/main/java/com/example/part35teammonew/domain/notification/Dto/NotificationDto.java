package com.example.part35teammonew.domain.notification.Dto;

import com.example.part35teammonew.domain.notification.Enum.NotificationType;
import com.example.part35teammonew.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
public class NotificationDto{
    private  String  id;
    private  Instant createdAt;
    private  Instant updatedAt;
    private  boolean confirmed;
    private  UUID userId;
    private  String content;
    private  NotificationType resourceType;
    private  UUID resourceId;

  public NotificationDto(Notification notification) {
    this.id = notification.getId().toHexString(); // <-- 여기 주목
    this.userId = notification.getUserId();
    this.createdAt = notification.getCreatedAt();
    this.updatedAt = notification.getUpdateAt();
    this.confirmed = notification.isConfirmed();
    this.content = notification.getContent();
    this.resourceType = notification.getType();
    this.resourceId = notification.getResourceId();
  }
}
