package com.example.part35teammonew.domain.comment.entity;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(
    name = "comment_like",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "comment_id"})
    } //동일한 사용자가 두 번 이상 좋아요 방지를 위한 유니크 제약 조건
    // comment_like 테이블에 같은 user_id와 comment_id를 동시에 가지는 데이터가 중복 저장되지 않도록
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentLike {

  @Id
  @GeneratedValue
  private UUID id; //댓글 좋아요의 고유 아이디

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id", nullable = false)
  private Article article; //연관된 게시글 아이디

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt; //좋아요 생성 시간

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "comment_id", nullable = false)
  private Comment comment; //연관된 댓글 아이디

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; //좋아요를 누른 사용자아이디 (한 댓글에 여러 사람이 좋아요를 누를 수 있기에)*/


  public static CommentLike create(Comment comment, User user) {
    return CommentLike.builder()
        .comment(comment)
        .user(user)
        .createdAt(LocalDateTime.now())
        .build();
  }

}