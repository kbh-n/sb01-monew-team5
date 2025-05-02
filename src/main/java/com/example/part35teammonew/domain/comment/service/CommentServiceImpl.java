package com.example.part35teammonew.domain.comment.service;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import com.example.part35teammonew.domain.comment.dto.CommentCreateRequest;
import com.example.part35teammonew.domain.comment.dto.CommentDto;
import com.example.part35teammonew.domain.comment.dto.CommentPageResponse;
import com.example.part35teammonew.domain.comment.dto.CommentUpdateRequest;
import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.comment.mapper.CommentMapper;
import com.example.part35teammonew.domain.comment.repository.CommentRepository;
import com.example.part35teammonew.domain.user.entity.User;
import com.example.part35teammonew.domain.user.repository.UserRepository;
import com.example.part35teammonew.domain.userActivity.maper.RecentCommentMapper;
import com.example.part35teammonew.domain.userActivity.service.UserActivityServiceInterface;
import com.example.part35teammonew.exeception.comment.CommentDeleteUnauthorized;
import com.example.part35teammonew.exeception.comment.CommentNotFound;
import com.example.part35teammonew.exeception.comment.CommentUpdateUnauthorized;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final ArticleRepository articleRepository;
  private final UserRepository userRepository;
  private final CommentLikeService commentLikeService;
  private final CommentMapper commentMapper;
  private final UserActivityServiceInterface userActivityServiceInterface;
  private final RecentCommentMapper recentCommentMapper;
  private static final int DEFAULT_LIMIT = 50; //api 명세상 50임

  @Override
  @Transactional
  public CommentDto createComment(CommentCreateRequest request, UUID requestUserId) {
    log.debug("댓글 생성 요청: userId={}, articleId={}", request.getUserId(), request.getArticleId());

    // 다른 엔드포인트에서는 요청자 ID가 필수적이지만 댓글 생성시에는 딱히 언급이 없어서 일단 추가하지만 필수는 아니도록 함.
    // 만일 입력된 요청자가 없을시 raw에 입력된 사용자 아이디를 사용하도록 함.
    final UUID effectiveUserId;
    if (requestUserId != null) {
      effectiveUserId = requestUserId;
    } else {
      effectiveUserId = request.getUserId();
    }

    // 사용자 ID 검증 로직
    if (!request.getUserId().equals(effectiveUserId)) {
      log.debug("사용자 ID 불일치: requestUserId={}, requestBodyUserId={}",
          effectiveUserId, request.getUserId());
      throw new CommentUpdateUnauthorized("요청자 ID와 댓글 작성자 ID가 일치하지 않습니다");
    }

    // 나머지 기존 로직은 동일하게 유지
    User user = userRepository.findById(effectiveUserId)
        .orElseThrow(() -> {
          log.debug("존재하지 않는 사용자: userId={}", effectiveUserId);
          return new IllegalArgumentException("존재하지 않는 사용자입니다"); //todo 유저 커스텀 예외로 수정
        });

    Article article = articleRepository.findById(request.getArticleId())
        .orElseThrow(() -> {
          log.debug("존재하지 않는 게시글: articleId={}", request.getArticleId());
          return new IllegalArgumentException("존재하지 않는 게시글입니다"); //todo 기사 커스텀 예외로 수정
        });
    //
    article.setCommentCount( article.getCommentCount() + 1);
    //
    Comment comment = commentMapper.toComment(request, user, article);

    Comment savedComment = commentRepository.save(comment);

    userActivityServiceInterface.addRecentCommentView(requestUserId,recentCommentMapper.toDto(comment));

    if (savedComment == null) {
      log.error("댓글 저장 실패: 저장된 댓글이 null입니다");
      throw new RuntimeException("댓글 저장에 실패했습니다");
    }

    log.info("댓글 생성 성공: commentId={}, userId={}", savedComment.getId(), effectiveUserId);

    return commentMapper.toCommentDto(savedComment, false);
  }

  @Override
  @Transactional
  public CommentDto updateComment(UUID commentId, CommentUpdateRequest request, UUID requestUserId) {
    log.debug("댓글 수정 요청: commentId={}, userId={}", commentId, requestUserId);

    // 댓글 조회
    Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
        .orElseThrow(() -> {
          log.debug("존재하지 않는 댓글: commentId={}", commentId);
          return new CommentNotFound("존재하지 않는 댓글입니다");
        });

    // 댓글 작성자와 요청자가 같은지 확인
    if (!comment.getUser().getId().equals(requestUserId)) {
      log.debug("댓글 수정 권한 없음: commentId={}, requestUserId={}, commentUserId={}",
          commentId, requestUserId, comment.getUser().getId());
      throw new CommentUpdateUnauthorized("댓글 작성자만 수정할 수 있습니다");
    }

    // 댓글 내용 업데이트
    comment.updateContent(request.getContent());

    // 변경사항 저장
    Comment updatedComment = commentRepository.save(comment);

    log.info("댓글 수정 성공: commentId={}, userId={}", commentId, requestUserId);

    // 해당 댓글에 대한 사용자의 좋아요 여부 확인
    boolean likedByMe = commentLikeService.hasLiked(commentId, requestUserId);

    // 매퍼를 사용하여 DTO 생성 및 반환
    return commentMapper.toCommentDto(updatedComment, likedByMe);
  }

  @Override
  @Transactional
  public boolean deleteComment(UUID commentId, UUID requestUserId) {
    log.debug("댓글 논리 삭제 요청: commentId={}, requestUserId={}", commentId, requestUserId);

    // 댓글 조회
    Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
        .orElseThrow(() -> {
          log.debug("존재하지 않는 댓글: commentId={}", commentId);
          return new CommentNotFound("존재하지 않는 댓글입니다");
        });

    // 댓글 작성자와 요청자가 같은지 확인 (권한 검증)
    if (!comment.getUser().getId().equals(requestUserId)) {
      log.debug("댓글 삭제 권한 없음: commentId={}, requestUserId={}, commentUserId={}",
          commentId, requestUserId, comment.getUser().getId());
      throw new CommentDeleteUnauthorized("댓글 작성자만 삭제할 수 있습니다");
    }

    //기사 CommentCount 감소
    Article article = comment.getArticle();
    article.setCommentCount( article.getCommentCount() - 1);

    // 댓글 논리 삭제 처리
    comment.delete();

    // 변경사항 저장
    commentRepository.save(comment);

    log.info("댓글 논리 삭제 성공: commentId={}", commentId);
    return true;
  }

  @Override
  @Transactional
  public boolean hardDeleteComment(UUID commentId) {
    log.debug("댓글 물리 삭제 요청: commentId={}", commentId);

    // 댓글 존재 여부 확인
    Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
        .orElseThrow(() -> {
          log.debug("존재하지 않는 댓글: commentId={}", commentId);
          return new CommentNotFound("존재하지 않는 댓글입니다");
        });

    //기사 CommentCount 감소 ( 논리 삭제 후 물리 삭제 시 -2 ? )
    Article article = comment.getArticle();
    article.setCommentCount( article.getCommentCount() - 1);

    // 댓글 물리 삭제
    commentRepository.delete(comment);

    log.info("댓글 물리 삭제 성공: commentId={}", commentId);
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public long countLikes(UUID commentId) {
    log.debug("댓글 좋아요 수 조회 요청: commentId={}", commentId);

    // 댓글 존재 여부 확인
    Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
        .orElseThrow(() -> {
          log.debug("존재하지 않는 댓글: commentId={}", commentId);
          return new CommentNotFound("존재하지 않는 댓글입니다");
        });

    // 댓글의 좋아요 개수 반환
    long likeCount = comment.getLikeCount();
    log.info("댓글 좋아요 수 조회 성공: commentId={}, likeCount={}", commentId, likeCount);
    return likeCount;
  }

  @Override
  @Transactional(readOnly = true)
  public CommentPageResponse getComments(
      UUID articleId,
      String orderBy,
      String direction,
      String cursor,
      LocalDateTime after,
      Integer limit,
      UUID requestUserId) {

    log.debug("댓글 목록 조회 요청: articleId={}, orderBy={}, direction={}, cursor={}, limit={}, requestUserId={}",
        articleId, orderBy, direction, cursor, limit, requestUserId);

    // 게시글 존재 여부 확인
    if (!articleRepository.existsById(articleId)) {
      log.debug("존재하지 않는 게시글: articleId={}", articleId);
      throw new IllegalArgumentException("존재하지 않는 게시글입니다");
    }

    // 기본값 설정 (50)
    if (limit == null || limit <= 0) {
      log.debug("기본 페이지 크기 설정: {}", DEFAULT_LIMIT);
      limit = DEFAULT_LIMIT;
    }

// 정렬 필드 설정
    String sortField;
    if (orderBy != null && (orderBy.equalsIgnoreCase("likes") || orderBy.equalsIgnoreCase("likeCount"))) {
      sortField = "likeCount";
    } else {
      sortField = "createdAt"; // 기본값은 여전히 createdAt
    }

// 정렬 방향 설정
    boolean isAscending;
    if (direction != null && direction.equalsIgnoreCase("asc")) {
      isAscending = true;
    } else {
      isAscending = false; // 기본값은 내림차순
    }

    String directionLog;
    if (isAscending) {
      directionLog = "오름차순"; // isAscending == true
    } else {
      directionLog = "내림차순"; // isAscending == false
    }
    log.debug("정렬 방향 설정: {}", directionLog);

    // 페이지 설정 (정렬 방향은 쿼리에서 처리하므로 여기서는 항상 ASC)
    Pageable pageable = PageRequest.of(0, limit);

    // 커서 ID 파싱
    UUID cursorId = null;
    if (cursor != null && !cursor.isEmpty()) {
      try {
        cursorId = UUID.fromString(cursor);
        log.debug("커서 ID 파싱 성공: cursorId={}", cursorId);
      } catch (IllegalArgumentException e) {
        log.debug("유효하지 않은 커서 값: cursor={}", cursor);
        throw new IllegalArgumentException("유효하지 않은 커서 값입니다");
      }
    }

    // 커서 기준 댓글 조회
    Comment cursorComment = null;
    Integer likesCursor = null;
    LocalDateTime effectiveAfter = after;

    // 커서가 있는 경우 해당 댓글 정보 조회
    if (cursorId != null) {

      Optional<Comment> optionalComment = commentRepository.findByIdAndIsDeletedFalse(cursorId);

      if (optionalComment.isPresent()) {
        cursorComment = optionalComment.get();
      } else {
        log.debug("커서에 해당하는 댓글을 찾을 수 없음: cursorId={}", cursorId);
        throw new CommentNotFound("커서에 해당하는 댓글을 찾을 수 없습니다");
      }

      likesCursor = cursorComment.getLikeCount();

      // after 값이 없는 경우 커서 댓글의 생성시간 사용
      if (effectiveAfter == null) {
        effectiveAfter = cursorComment.getCreatedAt();
      }
    }

    // 댓글 조회 (정렬 기준과 방향에 따라 다른 쿼리 사용)
    List<Comment> comments;
    if (sortField.equals("likeCount")) { // 좋아요 수로 정렬
      if (isAscending) { // 오름차순
        if (cursorId == null || likesCursor == null || effectiveAfter == null) {
          comments = commentRepository.findByArticleIdLikesAsc(articleId, pageable);
        } else {
          comments = commentRepository.findByArticleIdLikesAfterCursorWithValues(
              articleId, cursorId, likesCursor, effectiveAfter, pageable);
        }
      } else { // 내림차순
        if (cursorId == null || likesCursor == null || effectiveAfter == null) {
          comments = commentRepository.findByArticleIdLikesDesc(articleId, pageable);
        } else {
          comments = commentRepository.findByArticleIdLikesBeforeCursorWithValues(
              articleId, cursorId, likesCursor, effectiveAfter, pageable);
        }
      }
    } else { // 생성 시간으로 정렬 (기본값)
      if (isAscending) { // 오름차순
        if (cursorId == null || effectiveAfter == null) {
          comments = commentRepository.findByArticleIdCreatedAtAsc(articleId, pageable);
        } else {
          comments = commentRepository.findByArticleIdCreatedAtAfterCursorWithValues(
              articleId, cursorId, effectiveAfter, pageable);
        }
      } else { // 내림차순
        if (cursorId == null || effectiveAfter == null) {
          comments = commentRepository.findByArticleIdCreatedAtDesc(articleId, pageable);
        } else {
          comments = commentRepository.findByArticleIdCreatedAtBeforeCursorWithValues(
              articleId, cursorId, effectiveAfter, pageable);
        }
      }
    }


    log.debug("댓글 조회 결과: 조회된 댓글 수={}", comments.size());

    // 사용자가 각 댓글에 좋아요를 눌렀는지 확인하고 DTO로 변환
    List<CommentDto> commentDtos = comments.stream()
        .map(comment -> {
          boolean likedByMe;
          if (requestUserId != null) {
            likedByMe = commentLikeService.hasLiked(comment.getId(), requestUserId);
          } else {
            likedByMe = false;
          }
          return commentMapper.toCommentDto(comment, likedByMe);
        })
        .collect(Collectors.toList());

    // 다음 페이지 정보 설정
    String nextCursor = null;
    LocalDateTime nextAfter = null;
    boolean hasNext = false;

    if (!comments.isEmpty()) {
      Comment lastComment = comments.get(comments.size() - 1);
      nextCursor = lastComment.getId().toString();
      nextAfter = lastComment.getCreatedAt();

      // 다음 페이지 존재 여부 확인 (요청한 크기만큼 데이터가 있으면 다음 페이지 존재)
      if (comments.size() >= limit) {
        hasNext = true;
      } else {
        hasNext = false;
      }

      log.debug("다음 페이지 정보 설정: nextCursor={}, nextAfter={}, hasNext={}",
          nextCursor, nextAfter, hasNext);
    }

    // 전체 댓글 개수 조회
    long totalElements = commentRepository.countByArticleIdAndIsDeletedFalse(articleId);
    log.debug("전체 댓글 개수: articleId={}, totalElements={}", articleId, totalElements);

    // 응답 생성
    CommentPageResponse response = CommentPageResponse.builder()
        .content(commentDtos)
        .nextCursor(nextCursor)
        .nextAfter(nextAfter)
        .size(commentDtos.size())
        .totalElements(totalElements)
        .hasNext(hasNext)
        .build();

    log.info("댓글 목록 조회 완료: articleId={}, 조회된 댓글 수={}, 전체 댓글 수={}",
        articleId, commentDtos.size(), totalElements);

    return response;
  }

  @Override
  @Transactional(readOnly = true)
  public CommentDto getComment(UUID commentId, UUID requestUserId) {
    log.debug("단일 댓글 조회 요청: commentId={}, requestUserId={}", commentId, requestUserId);

    // 댓글 조회
    Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
        .orElseThrow(() -> {
          log.debug("존재하지 않는 댓글: commentId={}", commentId);
          return new CommentNotFound("존재하지 않는 댓글입니다");
        });

    // 사용자가 해당 댓글에 좋아요를 눌렀는지 확인
    boolean likedByMe;
    if (requestUserId != null) {
      likedByMe = commentLikeService.hasLiked(comment.getId(), requestUserId);
    } else {
      likedByMe = false;
    }

    log.debug("사용자 좋아요 여부 확인: commentId={}, requestUserId={}, likedByMe={}",
        commentId, requestUserId, likedByMe);

    // 매퍼를 사용하여 DTO 생성 및 반환
    CommentDto commentDto = commentMapper.toCommentDto(comment, likedByMe);

    log.info("단일 댓글 조회 완료: commentId={}", commentId);

    return commentDto;
  }
}