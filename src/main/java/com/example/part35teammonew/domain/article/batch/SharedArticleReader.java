package com.example.part35teammonew.domain.article.batch;

import com.example.part35teammonew.domain.article.api.NewsSearch;
import com.example.part35teammonew.domain.article.dto.ArticleBaseDto;
import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.interest.dto.request.InterestCreateRequest;
import com.example.part35teammonew.domain.interest.dto.response.InterestDto;
import com.example.part35teammonew.domain.interest.service.InterestService;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.batch.item.ItemReader;
@RequiredArgsConstructor
public class SharedArticleReader implements ItemReader<Article> {

  private final NewsSearch newsSearch;
  private final Queue<Article> queue = new LinkedList<>();
  private int page = 0;
  private final InterestService interestService;

  @Override
  public Article read() throws Exception {
    if (!queue.isEmpty()) {
      return queue.poll();
    }

    if (page >= 10) {
      return null;
    }
    //interestService.getInterests() 후 keyword 대체
    List<Pair<String, UUID>> interestList = interestService.getInterestList();
    if (interestList.isEmpty()) {
      //디폴트 값
      InterestDto interest = interestService.createInterest(
          new InterestCreateRequest("지역", List.of("인천", "대구")));
      interestList = interestService.getInterestList();
    }
    for (Pair<String, UUID> stringUUIDPair : interestList) {
      String keyword = stringUUIDPair.getKey();
      System.out.println("keyword = " + keyword);

      page++;
      String json = newsSearch.getNews(keyword, 10, (page - 1) * 10 + 1, "date");
      JSONArray items = new JSONObject(json).getJSONArray("items");

      for (int i = 0; i < items.length(); i++) {
        JSONObject item = items.getJSONObject(i);
        String title = item.getString("title").replaceAll("<.*?>", "");
        String summary = item.getString("description").replaceAll("<.*?>", "");
        String link = item.getString("link");
        String source = item.optString("originallink", "null");
        String pubDate = item.getString("pubDate");

        Article article = new Article(
            new ArticleBaseDto(null, title, summary, link, source, parsePubDate(pubDate),null, 0, null));
        queue.add(article);
      }
    }


    return queue.poll();
  }

  private LocalDateTime parsePubDate(String pubDate) {
    DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
    return ZonedDateTime.parse(pubDate, formatter).toLocalDateTime();
  }
}

