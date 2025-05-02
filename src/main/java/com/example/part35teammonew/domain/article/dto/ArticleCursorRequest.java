package com.example.part35teammonew.domain.article.dto;

import com.example.part35teammonew.domain.article.entity.Direction;
import com.example.part35teammonew.domain.article.entity.SortField;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArticleCursorRequest {
  private String cursor;
  private SortField sortField;
  private int size;
  private Direction direction;
  private List<ArticleBaseDto> articles;

  public ArticleCursorRequest(String cursor, SortField sortField, int limit, Direction direction, List<ArticleBaseDto> articles) {
    if(cursor != null){
      this.cursor = cursor;
    }else if(sortField == SortField.publishDate) {
      this.cursor = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }else{
      this.cursor = "0";
    }
    if( cursor == null && sortField == SortField.publishDate && direction == Direction.ASC ){
      this.cursor = LocalDateTime.of(1970,1,1,0,0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }else if(cursor == null && sortField == SortField.commentCount && direction == Direction.DESC){
      this.cursor = String.valueOf(Integer.MAX_VALUE);
    }
    if(limit == 0){
      throw new IllegalArgumentException("limit is 0");
    }
    this.sortField = sortField;
    this.size = limit;
    this.direction = direction;
    this.articles = articles;
  }
}
