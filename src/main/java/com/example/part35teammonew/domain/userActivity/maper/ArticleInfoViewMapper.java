package com.example.part35teammonew.domain.userActivity.maper;

import com.example.part35teammonew.domain.article.dto.ArticleEnrollmentResponse;
import com.example.part35teammonew.domain.articleView.service.ArticleViewServiceInterface;
import com.example.part35teammonew.domain.userActivity.Dto.ArticleInfoView;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArticleInfoViewMapper {

  private final ArticleViewServiceInterface articleViewService;

  ArticleInfoViewMapper(@Autowired ArticleViewServiceInterface articleViewService) {
    this.articleViewService = articleViewService;
  }

  public ArticleInfoView toDto(ArticleEnrollmentResponse article, UUID userId) {
    return ArticleInfoView.builder()
        .id(article.getId())//기사 아이디
        .viewedBy(userId)//본 사람
        .createdAt(Instant.now())//본시간
        .articleId(article.getId())
        .source(article.getSource())
        .sourceUrl(article.getSourceUrl())
        .articleTitle(article.getArticleTitle())
        .articlePublishedDate(article.getArticlePublishedDate())
        .articleSummary(article.getArticleSummary())
        .articleCommentCount(article.getArticleCommentCount())
        .articleViewCount(article.getArticleViewCount())//조회 메서드
        .build();
  }

}
