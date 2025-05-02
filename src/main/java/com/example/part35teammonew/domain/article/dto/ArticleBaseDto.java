package com.example.part35teammonew.domain.article.dto;

import com.example.part35teammonew.domain.article.entity.Article;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class ArticleBaseDto {
  private UUID id;

  @NotBlank
  private String title;

  @NotBlank
  private String summary;

  @NotBlank
  private String sourceUrl;

  @NotBlank
  private String source;

  @NotNull
  private LocalDateTime publishDate;

  private LocalDateTime createdAt;

  @NotNull
  private int commentCount;

  private Long viewCount;

  public ArticleBaseDto(Article article) {
    this.id = article.getId();
    this.title = article.getTitle();
    this.summary = article.getSummary();
    this.sourceUrl = article.getLink();
    this.source = article.getSource();
    this.publishDate = article.getDate();
    this.createdAt = article.getCreatedAt();
    this.commentCount = article.getCommentCount();
    this.viewCount = article.getViewCount();
  }

  // 생성자
  public ArticleBaseDto(UUID id, String title, String summary, String link, String source, LocalDateTime date, LocalDateTime createdAt, int commentCount, Long viewCount) {
    this.id = id;
    this.title = title;
    this.summary = summary;
    this.sourceUrl = link;
    this.source = source;
    this.publishDate = date;
    this.createdAt = createdAt;
    this.commentCount = commentCount;
    this.viewCount = viewCount;
  }
  // 저장 Dto
  public ArticleBaseDto(String title, String summary, String link, String source,
      LocalDateTime date, int commentCount) {
    this.title = title;
    this.summary = summary;
    this.sourceUrl = link;
    this.source = source;
    this.publishDate = date;
    this.commentCount = commentCount;
  }
}
