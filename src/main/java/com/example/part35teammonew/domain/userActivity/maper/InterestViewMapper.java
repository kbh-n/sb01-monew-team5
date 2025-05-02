package com.example.part35teammonew.domain.userActivity.maper;

import com.example.part35teammonew.domain.interest.dto.response.InterestDto;
import com.example.part35teammonew.domain.interestUserList.service.InterestUserListServiceInterface;
import com.example.part35teammonew.domain.userActivity.Dto.InterestView;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InterestViewMapper {

  private final InterestUserListServiceInterface interestUserListServiceInterface;

  public InterestViewMapper(
      @Autowired InterestUserListServiceInterface interestUserListServiceInterface) {
    this.interestUserListServiceInterface = interestUserListServiceInterface;
  }

  public InterestView toDto(InterestDto interest) {
    return InterestView.builder()
        .id(interest.getId())
        .interestId(interest.getId())
        .interestName(interest.getName())
        .interestKeywords(interest.getKeywords())
        .interestSubscriberCount(
            interest.getSubscriberCount())
        .createdAt(Instant.now())
        .build();
  }

}
