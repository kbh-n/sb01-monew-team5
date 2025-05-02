package com.example.part35teammonew.domain.userActivity.Dto;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeCommentView {

  private UUID id;
  private LocalDateTime createdAt;

  private UUID commentId;
  private UUID articleId;
  private String articleTitle;

  private UUID commentUserId;
  private String commentUserNickname;

  private String commentContent;
  private Integer commentLikeCount;
  private LocalDateTime commentCreatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LikeCommentView that = (LikeCommentView) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
