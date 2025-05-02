package com.example.part35teammonew.domain.comment.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeResponse {
  private UUID id;  // 좋아요 ID
  private UUID likedBy; // 좋아요를 누른 사용자 ID
  private UUID commentId; // 좋아요가 달린 댓글 ID
  private LocalDateTime createdAt; // 좋아요 생성 시간
}