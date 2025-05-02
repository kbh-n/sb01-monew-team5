package com.example.part35teammonew.domain.userActivity.maper;

import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.userActivity.Dto.RecentCommentView;
import org.springframework.stereotype.Component;

@Component
public class RecentCommentMapper {

  public RecentCommentView toDto(Comment comment) {
    return RecentCommentView.builder()
        .id(comment.getId())
        .articleId(comment.getArticle().getId())
        .articleTitle(comment.getArticle().getTitle())
        .userId(comment.getUser().getId())
        .userNickname(comment.getUserNickname())
        .content(comment.getContent())
        .likeCount(comment.getLikeCount())
        .createdAt(comment.getCreatedAt())
        .build();
  }
}
