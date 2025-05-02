package com.example.part35teammonew.domain.article.batch;

import com.example.part35teammonew.domain.article.api.NewsSearch;
import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import com.example.part35teammonew.domain.interest.service.InterestService;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class S3BatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;
  private final ArticleRepository articleRepository;
  private final NewsSearch newsSearch;
  private final S3UploadArticle s3UploadArticle;
  private final InterestService interestService;


  @Bean
  public Job S3BatchJob() {
    //JobBuilder("실행할 Job 이름", 작업 트래킹 > jobRepository)
    return new JobBuilder("S3BatchJob", jobRepository).start(S3articleStep()).build();
  }

  @Bean
  public Step S3articleStep() {
    System.out.println("S3BatchConfig");
    //10개씩 끊어서 처리
    return new StepBuilder("S3articleStep", jobRepository).<Article, Article>chunk(10,
            platformTransactionManager)
        .reader(S3articleReader())
        .processor(S3articleProcessor())
        .writer(articleWriterWithS3())
        .build();
  }

  @Bean
  @StepScope
  public ItemReader<Article> S3articleReader() {
    //모든 관심사를 찾아서 그 키워드를 Queue에 넣고 돌려서 갱신?
    //키워드가 너무 많으면 시간대 별로 나눠서 진행?
    return new SharedArticleReader(newsSearch,interestService);
    /*return new ItemReader<>() {
      private Queue<Article> queue = new LinkedList<>();
      private int page = 0;

      @Override
      public Article read() throws Exception {
        if (!queue.isEmpty()) {
          return queue.poll();
        }

        // 다 꺼냈다면 다음 뉴스 API 호출
        if (page >= 10) { // 최대 100개까지만 읽기 (10페이지 x 10개)
          return null;
        }

        page++;
        //키워드 파라미터로 받도록 수정 필요
        String json = newsSearch.getNews("금융", 10, (page - 1) * 10 + 1, "date");
        JSONObject jsonObject = new JSONObject(json);
        JSONArray items = jsonObject.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
          JSONObject item = items.getJSONObject(i);
          String title = item.getString("title").replaceAll("<.*?>", "");
          String summary = item.getString("description").replaceAll("<.*?>", "");
          String link = item.getString("link");
          //item이라는 JSON 객체에서 "originallink"라는 키가 존재하면 → 그 값을 쓰고, 없으면 "null"라는 기본값을 쓰겠다는 뜻
          String source = item.optString("originallink", "null");
          String pubDate = item.getString("pubDate");
          Article article = new Article(
              new ArticleBaseDto(null, title, summary, link, source, parsePubDate(pubDate), 0));
          queue.add(article);
        }
        return queue.poll();
      }
    };*/
  }


  @Bean
  public ItemProcessor<Article, Article> S3articleProcessor() {
    //중복 검사
    return article ->
        articleRepository.findByTitleAndDate(article.getTitle(), article.getDate()) != null ? null : article;
  }
  @Bean
  public ItemWriter<Article> articleWriterWithS3() {
    return chunk -> {
      List<? extends Article> articles = chunk.getItems();
      System.out.println("articles = " + articles);
      if (articles.isEmpty()) {
        System.out.println("No new articles");
        return;
      }
      try {
        String today = LocalDate.now().toString();
        File file = new File("articles_" + today + ".json");
        JSONArray jsonArray = new JSONArray();

        if (file.exists()) { //서버 내 파일
          String content = Files.readString(file.toPath());
          jsonArray = new JSONArray(content);
        }else  if( s3UploadArticle.exists(file.getName())){ // 기존 S3있는 파일
          s3UploadArticle.download(file);
          String content = Files.readString(file.toPath());
          jsonArray = new JSONArray(content);
        }else {
          jsonArray = new JSONArray(); //둘 다 없을 떄
        }

        Set<String> existingTitles = new HashSet<>();
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject obj = jsonArray.getJSONObject(i);
          existingTitles.add(obj.getString("title")); // 또는 다른 고유값 (링크 등)
        }
        int count = 0;

        for (Article article : articles) {
          if (existingTitles.contains(article.getTitle())) continue;

          JSONObject json = new JSONObject();
          json.put("id", article.getId());
          json.put("title", article.getTitle());
          json.put("summary", article.getSummary());
          json.put("link", article.getLink());
          json.put("source", article.getSource());
          json.put("date", article.getDate().toString());
          json.put("commentCount", article.getCommentCount());
          jsonArray.put(json);
          count++;
        }

        // 📦 파일 덮어쓰기
        try (FileWriter writer = new FileWriter(file)) {
          writer.write(jsonArray.toString(2));
        }

        // ☁️ S3 업로드
        s3UploadArticle.upload(file, file.getName());

        System.out.println("✅ JSON 파일 S3 업로드 완료 (누적): " + file.getName());
        System.out.println("추가된 기사 수: " + count);

        /*//파일 삭제
        if (!file.delete()) {
          System.err.println("⚠️ 파일 삭제 실패: " + file.getAbsolutePath());
        }*/
      } catch (Exception e) {
        throw new RuntimeException("JSON 파일 누적 저장 실패", e);
      }
    };
  }



}
