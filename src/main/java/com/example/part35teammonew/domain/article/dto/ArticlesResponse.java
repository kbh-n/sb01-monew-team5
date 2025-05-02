package com.example.part35teammonew.domain.article.dto;

import java.util.List;
import lombok.Data;

@Data
public class ArticlesResponse {
  List<ArticleBaseDto> content;
  String hasNext;
  String nextAfter;
  String nextCursor;
  int size;
  int totalElements;
}
