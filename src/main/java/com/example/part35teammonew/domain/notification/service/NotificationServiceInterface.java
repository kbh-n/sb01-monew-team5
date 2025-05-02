package com.example.part35teammonew.domain.notification.service;


import com.example.part35teammonew.domain.notification.Dto.CursorPageRequest;
import com.example.part35teammonew.domain.notification.Dto.CursorPageResponse;
import com.example.part35teammonew.domain.notification.Dto.NotificationDto;
import java.util.UUID;
import org.bson.types.ObjectId;

public interface NotificationServiceInterface {

  NotificationDto addNewsNotice(UUID userId, String content, UUID resourceId);//뉴스 알림 추가할떄

  NotificationDto addCommentNotice(UUID userId, String content, UUID resourceId);//댓글 관련 알림 추가할떄 했음

  boolean confirmedReadNotice(ObjectId id, UUID userId);//알림 아이디로 읽음 처리하기, 유저 아이디로로 교차 검증

  boolean confirmedReadAllNotice(UUID userId);// 유저의 알림 다 읽음처리

  boolean deleteOldConfirmedNotice();// 일주일 지난거 삭제, 따로 실행하는 서비스 있음

  //페이지 내이션 일단 안 읽은거만 내보냄, 그다음 시간 등으로 페이지네이션
  CursorPageResponse<NotificationDto> getNoticePage( UUID userId, CursorPageRequest pageRequest);


}
