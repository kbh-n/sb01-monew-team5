package com.example.part35teammonew.domain.article.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArticleEnrollmentResponse {
  //Vaildation 필요
  private UUID id;
  private UUID viewedBy;
  private LocalDate createdAt;
  private UUID articleId;
  private String source;
  private String sourceUrl;
  private String articleTitle;
  private LocalDateTime articlePublishedDate;
  private String articleSummary;
  private int articleCommentCount;
  private Long articleViewCount;

  public ArticleEnrollmentResponse(UUID id, UUID viewedBy, LocalDate createdAt, UUID articleId,
      String source, String sourceUrl, String articleTitle, LocalDateTime articlePublishedDate,
      String articleSummary, int articleCommentCount, Long articleViewCount) {
    this.id = id;
    this.viewedBy = viewedBy;
    this.createdAt = createdAt;
    this.articleId = articleId;
    this.source = source;
    this.sourceUrl = sourceUrl;
    this.articleTitle = articleTitle;
    this.articlePublishedDate = articlePublishedDate;
    this.articleSummary = articleSummary;
    this.articleCommentCount = articleCommentCount;
    this.articleViewCount = articleViewCount;
  }
}
