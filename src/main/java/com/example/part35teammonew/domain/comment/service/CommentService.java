package com.example.part35teammonew.domain.comment.service;

import com.example.part35teammonew.domain.comment.dto.CommentCreateRequest;
import com.example.part35teammonew.domain.comment.dto.CommentDto;
import com.example.part35teammonew.domain.comment.dto.CommentPageResponse;
import com.example.part35teammonew.domain.comment.dto.CommentUpdateRequest;

import java.time.LocalDateTime;
import java.util.UUID;
  //댓글 생성
  //댓글 수정
  //댓글 삭제 (논리)
  //댓글 삭제 (물리)
  //목록 조회 (날짜순 정렬, 좋아요순 정렬)
  //단일 댓글 조회

  public interface CommentService {

    //댓글 생성
    CommentDto createComment(CommentCreateRequest request, UUID requestUserId);

    //댓글 수정
    CommentDto updateComment(UUID commentId, CommentUpdateRequest request, UUID requestUserId);

    //댓글 논리 삭제
    boolean deleteComment(UUID commentId, UUID requestUserId);

    //댓글 물리 삭제
    boolean hardDeleteComment(UUID commentId);

    //댓글의 좋아요 개수 조회
    long countLikes(UUID commentId);

    //댓글 목록 조회 (정렬 및 페이지네이션)
    CommentPageResponse getComments(
        UUID articleId,
        String orderBy,
        String direction,
        String cursor,
        LocalDateTime after,
        Integer limit,
        UUID requestUserId);

    //댓글 개별 조회
    CommentDto getComment(UUID commentId, UUID requestUserId);

  }
