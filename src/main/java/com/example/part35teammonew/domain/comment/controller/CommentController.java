package com.example.part35teammonew.domain.comment.controller;

import com.example.part35teammonew.domain.comment.dto.CommentCreateRequest;
import com.example.part35teammonew.domain.comment.dto.CommentDto;
import com.example.part35teammonew.domain.comment.dto.CommentPageResponse;
import com.example.part35teammonew.domain.comment.dto.CommentUpdateRequest;
import com.example.part35teammonew.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  //댓글 목록 조회
  @GetMapping
  public ResponseEntity<CommentPageResponse> getComments(
      @RequestParam(required = false) UUID articleId, //기사 아이디
      @RequestParam(defaultValue = "createdAt") String orderBy, //정렬 속성(날짜, 좋아요 수) (프로토타입상 기본은 날짜)
      @RequestParam(defaultValue = "DESC") String direction, //정렬 기본 방향: 내림차순 (최신 댓글이 위로 감)
      @RequestParam(required = false) String cursor, // 커서 기반 페이지네이션 기준이 되는 댓글 ID
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam(defaultValue = "10") Integer limit, // 조회할 댓글 수 제한
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId) { // 요청 사용자 ID (헤더)

    log.info("댓글 목록 조회 요청: articleId={}, orderBy={}, direction={}, cursor={}, after={}, limit={}, requestUserId={}",
        articleId, orderBy, direction, cursor, after, limit, requestUserId);

    // articleId가 필수적으로 필요한지 검증
    if (articleId == null) {
      log.debug("기사 ID 누락");
      throw new IllegalArgumentException("기사 ID는 필수입니다."); //기사 관련 예외처리
    }

    // 허용된 정렬 필드만 사용
    if (!orderBy.equalsIgnoreCase("createdAt") && !orderBy.equalsIgnoreCase("likeCount")) {
      log.debug("유효하지 않은 정렬 필드({}), 기본값(createdAt)으로 설정", orderBy);
      orderBy = "createdAt";
    }

    // 정렬 방향 검증
    if (!direction.equalsIgnoreCase("ASC") && !direction.equalsIgnoreCase("DESC")) {
      log.debug("유효하지 않은 정렬 방향({}), 기본값(DESC)으로 설정", direction);
      direction = "DESC";
    }

    // 페이지 크기 50으로 제한 (api 명세에 따라서)
    if (limit <= 0 || limit > 100) {
      log.debug("유효하지 않은 페이지 크기({}), 기본값(50)으로 설정", limit);
      limit = 50;
    }

    CommentPageResponse response = commentService.getComments(
        articleId, orderBy, direction, cursor, after, limit, requestUserId);

    log.debug("댓글 목록 조회 완료: articleId={}, 조회된 댓글 수={}", articleId, response.getContent().size());
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody CommentCreateRequest request) { //CommentCreateRequest(articleId, userId, content)
    //헤더 제외함 (api상 생성에는 필요없음)

    log.info("댓글 생성 요청: articleId={}, userId={}",
        request.getArticleId(), request.getUserId());

    // 요청 본문의 userId를 직접 사용
    UUID userId = request.getUserId();
    CommentDto createdComment = commentService.createComment(request, userId);

    log.debug("댓글 생성 완료: commentId={}, articleId={}, userId={}",
        createdComment.getId(), request.getArticleId(), userId);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdComment);
  }

  //댓글 수정
  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentDto> updateComment(
      @PathVariable UUID commentId, //댓글 아이디
      @Valid @RequestBody CommentUpdateRequest request, //댓글 수정 요청
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId) { //요청자 아이디 헤더로 받기

    log.info("댓글 수정 요청: commentId={}, requestUserId={}, content={}",
        commentId, requestUserId, request.getContent());

    CommentDto updatedComment = commentService.updateComment(commentId, request, requestUserId);

    log.debug("댓글 수정 완료: commentId={}, userId={}", commentId, requestUserId);

    return ResponseEntity.ok(updatedComment);
  }

  //댓글 논리 삭제 (isdeleted를 true로)
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable UUID commentId, //댓글 아이디
      @RequestHeader("Monew-Request-User-ID") UUID requestUserId) { //요청자 아이디

    log.info("댓글 논리 삭제 요청: commentId={}, requestUserId={}", commentId, requestUserId);

    commentService.deleteComment(commentId, requestUserId);

    log.debug("댓글 논리 삭제 완료: commentId={}", commentId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> hardDeleteComment(@PathVariable UUID commentId) { //댓글 아이디
    log.info("댓글 물리 삭제 요청: commentId={}", commentId);

    commentService.hardDeleteComment(commentId);

    log.debug("댓글 물리 삭제 완료: commentId={}", commentId);
    return ResponseEntity.noContent().build();
  }
}