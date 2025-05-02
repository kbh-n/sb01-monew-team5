package com.example.part35teammonew.domain.comment.mapper;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.comment.dto.CommentCreateRequest;
import com.example.part35teammonew.domain.comment.dto.CommentDto;
import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

  public Comment toComment(CommentCreateRequest request, User user, Article article) {
    return Comment.create( //댓글 생성시
        request.getContent(),
        user.getNickname(),
        user,
        article
    );
  }

  public CommentDto toCommentDto(Comment comment, boolean likedByMe) {
    return CommentDto.builder()
        .id(comment.getId())
        .articleId(comment.getArticle().getId())
        .articleTitle(comment.getArticle().getTitle()) // 여기에 제목 추가 comment.getArticleTitle
        .userId(comment.getUser().getId())
        .userNickname(comment.getUserNickname())
        .content(comment.getContent())
        .likeCount(comment.getLikeCount())
        .likedByMe(likedByMe)
        .createdAt(comment.getCreatedAt())
        .build();
  }
}