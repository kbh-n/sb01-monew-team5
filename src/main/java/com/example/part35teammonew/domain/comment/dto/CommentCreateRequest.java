package com.example.part35teammonew.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCreateRequest {
  @NotNull(message = "기사 ID는 필수입니다")
  private UUID articleId;

  @NotNull(message = "사용자 ID는 필수입니다")
  private UUID userId;

  @NotBlank(message = "댓글 내용은 필수입니다")
  @Size(max = 500, message = "댓글은 500자를 초과할 수 없습니다")
  private String content;
}