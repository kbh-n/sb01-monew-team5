package com.example.part35teammonew.domain.comment.service;

import com.example.part35teammonew.domain.comment.dto.CommentDto;
import com.example.part35teammonew.domain.comment.dto.CommentLikeResponse;
import java.util.UUID;

public interface CommentLikeService {
  //좋아요 등록
  //좋아요 삭제
  //좋아요 여부 확인
  //좋아요 개수 조회

  //댓글 좋아요 추가
  CommentLikeResponse addLike(UUID commentId, UUID requestUserId);

  //댓글 좋아요 취소
  boolean removeLike(UUID commentId, UUID requestUserId);

  //사용자가 댓글에 좋아요를 눌렀는지 확인
  boolean hasLiked(UUID commentId, UUID userId);

  //좋아요 개별 조회
  CommentDto getCommentlike(UUID commentId, UUID requestUserId);

}
