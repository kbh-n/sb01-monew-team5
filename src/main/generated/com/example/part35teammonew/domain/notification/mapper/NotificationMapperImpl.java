package com.example.part35teammonew.domain.notification.mapper;

import com.example.part35teammonew.domain.notification.Dto.NotificationDto;
import com.example.part35teammonew.domain.notification.Enum.NotificationType;
import com.example.part35teammonew.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-28T16:35:21+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationDto toDto(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        ObjectId id = null;
        UUID userId = null;
        Instant createdAt = null;
        Instant updateAt = null;
        boolean confirmed = false;
        String content = null;
        NotificationType type = null;
        UUID resourceId = null;

        id = notification.getId();
        userId = notification.getUserId();
        createdAt = notification.getCreatedAt();
        updateAt = notification.getUpdateAt();
        confirmed = notification.isConfirmed();
        content = notification.getContent();
        type = notification.getType();
        resourceId = notification.getResourceId();

        NotificationDto notificationDto = new NotificationDto( id, userId, createdAt, updateAt, confirmed, content, type, resourceId );

        return notificationDto;
    }
}
