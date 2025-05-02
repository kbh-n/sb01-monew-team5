package com.example.part35teammonew.domain.userActivity.Dto;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecentCommentView {

  private UUID id; //코맨트 아이디
  private UUID articleId; //기사 아이디
  private String articleTitle; // 기사 제목

  private UUID userId; // 작성한 아이디
  private String userNickname; //닉네임

  private String content; //내영
  private Integer likeCount; //좋아요 수
  private LocalDateTime createdAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RecentCommentView that = (RecentCommentView) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
