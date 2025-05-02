package com.example.part35teammonew.domain.comment.repository;

import com.example.part35teammonew.domain.comment.entity.Comment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

  // 기존 메서드들 유지
  List<Comment> findByIsDeletedFalse();
  List<Comment> findByArticleIdAndIsDeletedFalse(UUID articleId);
  List<Comment> findByUserIdAndIsDeletedFalse(UUID userId);
  Optional<Comment> findByIdAndIsDeletedFalse(UUID id);

  //--- 생성 시간 기준
  // 커서 없이 생성 시간 기준 논리 삭제되지 않은 애들 중 내림차순
  @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false " +
      "ORDER BY c.createdAt DESC")
  List<Comment> findByArticleIdCreatedAtDesc(
      @Param("articleId") UUID articleId,
      Pageable pageable);

  // 커서 없이 생성 시간 기준 논리 삭제되지 않은 애들 중 오름차순
  @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false " +
      "ORDER BY c.createdAt ASC")
  List<Comment> findByArticleIdCreatedAtAsc(
      @Param("articleId") UUID articleId,
      Pageable pageable);

  // 커서 있는 생성 시간 기준 논리 삭제되지 않은 애들 중 내림차순
  @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false " +
      "AND c.id != :cursorId AND c.createdAt < :cursorDate " +
      "ORDER BY c.createdAt DESC")
  List<Comment> findByArticleIdCreatedAtBeforeCursorWithValues(
      @Param("articleId") UUID articleId,
      @Param("cursorId") UUID cursorId,
      @Param("cursorDate") LocalDateTime cursorDate,
      Pageable pageable);

  // 커서 있는 생성 시간 기준 논리 삭제되지 않은 애들 중 오름차순
  @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false " +
      "AND c.id != :cursorId AND c.createdAt > :cursorDate " +
      "ORDER BY c.createdAt ASC")
  List<Comment> findByArticleIdCreatedAtAfterCursorWithValues(
      @Param("articleId") UUID articleId,
      @Param("cursorId") UUID cursorId,
      @Param("cursorDate") LocalDateTime cursorDate,
      Pageable pageable);


  //--- 좋아요 수 기준
  // 커서 없이 좋아요 수 기준 논리 삭제되지 않은 애들 중 내림차순
  @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false " +
      "ORDER BY c.likeCount DESC, c.createdAt DESC")
  List<Comment> findByArticleIdLikesDesc(
      @Param("articleId") UUID articleId,
      Pageable pageable);

  // 커서 없이 좋아요 수 기준 논리 삭제되지 않은 애들 중 오름차순
  @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false " +
      "ORDER BY c.likeCount ASC, c.createdAt ASC")
  List<Comment> findByArticleIdLikesAsc(
      @Param("articleId") UUID articleId,
      Pageable pageable);

  // 커서 있는 좋아요 수 기준 논리 삭제되지 않은 애들 중 내림차순
  @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false " +
      "AND c.id != :cursorId AND (c.likeCount < :cursorLikes " +
      "     OR (c.likeCount = :cursorLikes AND c.createdAt < :cursorDate)) " +
      "ORDER BY c.likeCount DESC, c.createdAt DESC")
  List<Comment> findByArticleIdLikesBeforeCursorWithValues(
      @Param("articleId") UUID articleId,
      @Param("cursorId") UUID cursorId,
      @Param("cursorLikes") Integer cursorLikes,
      @Param("cursorDate") LocalDateTime cursorDate,
      Pageable pageable);

  // 커서 있는 좋아요 수 기준 논리 삭제되지 않은 애들 중 오름차순
  @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId AND c.isDeleted = false " +
      "AND c.id != :cursorId AND (c.likeCount > :cursorLikes " +
      "     OR (c.likeCount = :cursorLikes AND c.createdAt > :cursorDate)) " +
      "ORDER BY c.likeCount ASC, c.createdAt ASC")
  List<Comment> findByArticleIdLikesAfterCursorWithValues(
      @Param("articleId") UUID articleId,
      @Param("cursorId") UUID cursorId,
      @Param("cursorLikes") Integer cursorLikes,
      @Param("cursorDate") LocalDateTime cursorDate,
      Pageable pageable);

  // 댓글 개수 조회
  long countByArticleIdAndIsDeletedFalse(UUID articleId);
}