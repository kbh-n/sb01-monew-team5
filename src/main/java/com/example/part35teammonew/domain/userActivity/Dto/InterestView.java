package com.example.part35teammonew.domain.userActivity.Dto;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class InterestView {

  private UUID id;          //여기 그대로 interestId 넣으면 됨, 인덱스 설정해서 괜찮음 원래는 이거 고유 아이디 같음
  private UUID interestId;
  private String interestName;
  private List<String> interestKeywords; //,로 구분되는거 자르기
  private Long interestSubscriberCount;
  private Instant createdAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InterestView that = (InterestView) o;
    return Objects.equals(interestId, that.interestId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(interestId);
  }
}
