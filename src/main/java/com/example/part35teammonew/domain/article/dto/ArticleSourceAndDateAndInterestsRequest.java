package com.example.part35teammonew.domain.article.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArticleSourceAndDateAndInterestsRequest {
  private String keyword;
  private String publishDateFrom ;
  private String publishDateTo ;
  private String[] sourceIn ;

  public ArticleSourceAndDateAndInterestsRequest(String keyword, String publishDateFrom,
      String publishDateTo, String[] sourceIn) {
    this.keyword = keyword;
    this.publishDateFrom = publishDateFrom;
    this.publishDateTo = publishDateTo;
    this.sourceIn = sourceIn;
  }
}
