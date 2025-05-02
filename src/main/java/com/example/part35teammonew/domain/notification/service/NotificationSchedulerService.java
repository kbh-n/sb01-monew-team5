package com.example.part35teammonew.domain.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationSchedulerService {

  private final NotificationServiceInterface notificationService;

  public NotificationSchedulerService(@Autowired NotificationServiceInterface notificationService) {
    this.notificationService = notificationService;
  }

  @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
  public void runDeleteOldConfirmedNotice() {
    boolean result = notificationService.deleteOldConfirmedNotice();
    if (result) {
      //로그 남기기
    } else {
      //결과 없ㅇ므
    }
  }
}
