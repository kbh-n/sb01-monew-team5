package com.example.part35teammonew.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageResponse {
  private List<CommentDto> content;  // 페이지 내 댓글 목록
  private String nextCursor;   // 다음 페이지 조회를 위한 커서 값 (마지막 댓글의 ID 등)
  private LocalDateTime nextAfter;  // 다음 페이지 조회를 위한 시간 기준점
  private Integer size;     // 현재 페이지 크기 (댓글 개수)
  private long totalElements;    // 전체 댓글 개수
  private boolean hasNext;    // 다음 페이지 존재 여부
}