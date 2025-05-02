package com.example.part35teammonew.domain.article.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NewsSearchTest {
  @Test
  public void getNews(){
    NewsSearch newsSearch = new NewsSearch();
    String keyword = "금융";
    int display = 10;
    int start = 1;
    String sort = "sim";
    //
    String news = newsSearch.getNews(keyword, display, start, sort);
    //
    Assertions.assertThat(news).isNotNull();
  }

}