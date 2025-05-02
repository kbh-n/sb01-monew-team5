package com.example.part35teammonew.domain.userActivity.maper;

import com.example.part35teammonew.domain.comment.dto.CommentDto;
import com.example.part35teammonew.domain.comment.dto.CommentLikeResponse;
import com.example.part35teammonew.domain.userActivity.Dto.LikeCommentView;
import org.springframework.stereotype.Component;

@Component
public class LikeCommentMapper {

  public LikeCommentView toDto(CommentDto comment,
      CommentLikeResponse commentLike) {
    return LikeCommentView.builder()
        .id(comment.getId())
        .createdAt(comment.getCreatedAt())
        .articleId(comment.getArticleId())
        .articleTitle(comment.getArticleTitle())
        .commentUserId(comment.getUserId())
        .commentUserNickname(comment.getUserNickname())
        .commentContent(comment.getContent())
        .commentLikeCount(comment.getLikeCount())
        .commentCreatedAt(commentLike.getCreatedAt())
        .build();

  }

}
