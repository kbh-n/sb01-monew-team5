package com.example.part35teammonew.domain.comment.service;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import com.example.part35teammonew.domain.comment.dto.CommentDto;
import com.example.part35teammonew.domain.comment.dto.CommentLikeResponse;
import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.comment.entity.CommentLike;
import com.example.part35teammonew.domain.comment.mapper.CommentMapper;
import com.example.part35teammonew.domain.comment.repository.CommentLikeRepository;
import com.example.part35teammonew.domain.comment.repository.CommentRepository;
import com.example.part35teammonew.domain.notification.service.NotificationServiceInterface;
import com.example.part35teammonew.domain.user.entity.User;
import com.example.part35teammonew.domain.user.repository.UserRepository;

import com.example.part35teammonew.domain.userActivity.maper.LikeCommentMapper;
import com.example.part35teammonew.domain.userActivity.service.UserActivityServiceInterface;
import com.example.part35teammonew.exeception.comment.CommentLikeConflict;
import com.example.part35teammonew.exeception.comment.CommentLikeNotFound;
import com.example.part35teammonew.exeception.comment.CommentNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

  private final CommentLikeRepository commentLikeRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;
  private final CommentMapper commentMapper;
  private final NotificationServiceInterface notificationServiceInterface;
  private final UserActivityServiceInterface userActivityServiceInterface;
  private final LikeCommentMapper likeCommentMapper;

  @Override
  @Transactional
  public CommentLikeResponse addLike(UUID commentId, UUID requestUserId) {
    log.debug("댓글 좋아요 추가 요청: commentId={}, userId={}", commentId, requestUserId);

    // 댓글 존재 여부 확인
    Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
        .orElseThrow(() -> {
          log.debug("존재하지 않는 댓글: commentId={}", commentId);
          return new CommentNotFound("존재하지 않는 댓글입니다");
        });

    // 좋아요 요청자 존재 여부 확인
    User user = userRepository.findById(requestUserId)
        .orElseThrow(() -> {
          log.debug("존재하지 않는 사용자: userId={}", requestUserId);
          return new IllegalArgumentException("존재하지 않는 사용자입니다"); //todo 유저 커스텀 예외처리 나중에 추가
        });

    String requestName=user.getNickname();

    // 이미 좋아요를 눌렀는지 확인
    Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(requestUserId, commentId);
    if (existingLike.isPresent()) {
      log.debug("이미 좋아요를 누른 댓글: commentId={}, userId={}", commentId, requestUserId);
      throw new CommentLikeConflict("이미 좋아요를 누른 댓글입니다");
    }

    // 좋아요 생성
    CommentLike commentLike = CommentLike.create(comment, user);
    log.debug("댓글 좋아요 객체 생성: commentId={}, userId={}", commentId, requestUserId);

    // 관련 게시글 설정
    Article article = comment.getArticle();
    commentLike.setArticle(article);
    log.debug("좋아요에 게시글 정보 설정: articleId={}", article.getId());

    // 저장
    CommentLike savedLike = commentLikeRepository.save(commentLike);
    log.debug("좋아요 저장 완료: likeId={}", savedLike.getId());

    // 댓글의 좋아요 수 증가
    comment.incrementLikeCount();
    commentRepository.save(comment);
    log.debug("댓글 좋아요 수 증가: commentId={}, 현재 좋아요 수={}", commentId, comment.getLikeCount());

    // 응답 생성
    CommentLikeResponse response = CommentLikeResponse.builder()
        .id(savedLike.getId())
        .likedBy(user.getId())
        .commentId(comment.getId())
        .createdAt(savedLike.getCreatedAt())
        .build();

    userActivityServiceInterface.addLikeCommentView(comment.getUser().getId(), likeCommentMapper.toDto(
        commentMapper.toCommentDto(comment,hasLiked(comment.getId(),requestUserId)),response));

    log.info("댓글 좋아요 추가 성공: commentId={}, userId={}, likeId={}",
        commentId, requestUserId, savedLike.getId());
    notificationServiceInterface.addCommentNotice(comment.getUser().getId(), requestName+ " 님이 나의 댓글을 좋아합니다.", commentId);//좋아요 알림 생성

    return response;
  }

  @Override
  @Transactional
  public boolean removeLike(UUID commentId, UUID requestUserId) {
    log.debug("댓글 좋아요 삭제 요청: commentId={}, userId={}", commentId, requestUserId);

    // 댓글 존재 여부 확인
    Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
        .orElseThrow(() -> {
          log.debug("존재하지 않는 댓글: commentId={}", commentId);
          return new CommentNotFound("존재하지 않는 댓글입니다");
        });

    // 좋아요 존재 여부 확인
    CommentLike commentLike = commentLikeRepository.findByUserIdAndCommentId(requestUserId, commentId)
        .orElseThrow(() -> {
          log.debug("좋아요를 누르지 않은 댓글: commentId={}, userId={}", commentId, requestUserId);
          return new CommentLikeNotFound("해당 댓글에 좋아요를 누르지 않았습니다");
        });

    // 좋아요 삭제
    UUID likeId = commentLike.getId();
    commentLikeRepository.delete(commentLike);
    log.debug("댓글 좋아요 삭제 완료: likeId={}", likeId);

    // 댓글의 좋아요 수 감소
    comment.decrementLikeCount();
    commentRepository.save(comment);
    log.debug("댓글 좋아요 수 감소: commentId={}, 현재 좋아요 수={}", commentId, comment.getLikeCount());

    log.info("댓글 좋아요 삭제 성공: commentId={}, userId={}", commentId, requestUserId);
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasLiked(UUID commentId, UUID userId) {
    log.debug("댓글 좋아요 확인 요청: commentId={}, userId={}", commentId, userId);

    if (userId == null) {
      log.debug("사용자 ID가 null이므로 좋아요 안 누름으로 처리");
      return false;
    }

    boolean hasLiked = commentLikeRepository.findByUserIdAndCommentId(userId, commentId).isPresent();
    log.debug("댓글 좋아요 확인 결과: commentId={}, userId={}, hasLiked={}", commentId, userId, hasLiked);
    return hasLiked;
  }

  @Override
  @Transactional(readOnly = true)
  public CommentDto getCommentlike(UUID commentId, UUID requestUserId) {
    log.debug("댓글 좋아요 정보 조회 요청: commentId={}, userId={}", commentId, requestUserId);

    // 댓글 존재 여부 확인
    Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
        .orElseThrow(() -> {
          log.debug("존재하지 않는 댓글: commentId={}", commentId);
          return new CommentNotFound("존재하지 않는 댓글입니다");
        });

    // 사용자가 좋아요를 눌렀는지 확인
    boolean likedByMe = hasLiked(commentId, requestUserId);
    log.debug("사용자 좋아요 여부: commentId={}, userId={}, likedByMe={}",
        commentId, requestUserId, likedByMe);

    // CommentDto 생성 및 반환
    CommentDto commentDto = commentMapper.toCommentDto(comment, likedByMe);
    log.info("댓글 좋아요 정보 조회 완료: commentId={}, userId={}", commentId, requestUserId);
    return commentDto;
  }
}