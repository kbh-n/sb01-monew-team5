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
public class CommentDto {
  private UUID id; //댓글 아이디
  private UUID articleId; //기사 아이디
  private String articleTitle; //기사 제목 (추가된 필드)
  private UUID userId; //사용자 아이디
  private String userNickname; //사용자 닉네임
  private String content; //사용자 댓글
  private Integer likeCount; //좋아요 댓글 카운트
  private boolean likedByMe; //스스로 좋아요를 눌렀는가
  private LocalDateTime createdAt; //생성시간
  //수정시간 굳이 필요 없을 거 같음
}