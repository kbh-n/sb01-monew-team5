package com.example.part35teammonew.domain.comment.repository;

import com.example.part35teammonew.domain.comment.entity.CommentLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

  // 특정 댓글에 대해 유저가 이미 좋아요를 눌렀는지 여부 확인
  // → 유저 연동되면 주석 해제
  Optional<CommentLike> findByUserIdAndCommentId(UUID userId, UUID commentId);

  // 댓글 기준으로 좋아요 개수
  long countByCommentId(UUID commentId);

  // 좋아요 전체 삭제 (댓글 삭제시 사용 가능)
  void deleteAllByCommentId(UUID commentId);
}