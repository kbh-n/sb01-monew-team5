package com.example.part35teammonew.domain.comment.controller;

import com.example.part35teammonew.domain.comment.dto.CommentLikeResponse;
import com.example.part35teammonew.domain.comment.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentLikeController {

  private final CommentLikeService commentLikeService;

  //댓글 좋아요 등록
  @PostMapping("/{commentId}/comment-likes")
  public ResponseEntity<CommentLikeResponse> addLike(
      @PathVariable UUID commentId, //댓글 아이디
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId) { //요청자 아이디 (헤더)

    log.info("댓글 좋아요 등록 요청: commentId={}, userId={}", commentId, requestUserId);

    CommentLikeResponse response = commentLikeService.addLike(commentId, requestUserId);

    log.debug("댓글 좋아요 등록 완료: commentId={}, userId={}, likeId={}",
        commentId, requestUserId, response.getId());
    return ResponseEntity.ok(response);
  }

  //댓글 좋아요 취소
  @DeleteMapping("/{commentId}/comment-likes")
  public ResponseEntity<Void> removeLike(
      @PathVariable UUID commentId, //댓글 아이디
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId) { //요청자 아이디 (헤더)

    log.info("댓글 좋아요 취소 요청: commentId={}, userId={}", commentId, requestUserId);

    commentLikeService.removeLike(commentId, requestUserId);

    log.debug("댓글 좋아요 취소 완료: commentId={}, userId={}", commentId, requestUserId);
    return ResponseEntity.noContent().build();
  }
}