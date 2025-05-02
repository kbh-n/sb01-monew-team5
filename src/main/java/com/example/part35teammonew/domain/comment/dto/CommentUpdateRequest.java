package com.example.part35teammonew.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentUpdateRequest {
  @NotBlank(message = "댓글 내용은 필수입니다")
  @Size(max = 500, message = "댓글은 500자를 초과할 수 없습니다")
  private String content;
}