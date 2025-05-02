package com.example.part35teammonew.domain.article.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class findByCursorPagingResponse {
  private List<ArticleBaseDto> articles;
  private String nextCursor;
  private LocalDateTime nextAfter;
  private int limit;
  private String hasNext;
}
