package com.example.part35teammonew.domain.article.batch;

import com.example.part35teammonew.domain.article.api.NewsSearch;
import com.example.part35teammonew.domain.article.dto.ArticleBaseDto;
import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import com.example.part35teammonew.domain.article.service.ArticleService;
import com.example.part35teammonew.domain.interest.service.InterestService;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;
  private final ArticleRepository articleRepository;
  private final ArticleService articleService;
  private final NewsSearch newsSearch;
  private final InterestService interestService;
  //private final S3UploadArticle s3UploadArticle;


  @Bean
  public Job articleJob() {
    //JobBuilder("실행할 Job 이름", 작업 트래킹 > jobRepository)
    return new JobBuilder("articleJob", jobRepository).start(articleStep()).build();
  }

  @Bean
  public Step articleStep() {
    System.out.println("BatchConfig");
    //10개씩 끊어서 처리
    return new StepBuilder("articleStep", jobRepository).<Article, Article>chunk(10,
            platformTransactionManager)
        .reader(articleReader())
        .processor(articleProcessor())
        .writer(articleWriterWithDBAndFileSync())
        .build();
  }

  @Bean
  @StepScope
  public ItemReader<Article> articleReader() {
    return new SharedArticleReader(newsSearch, interestService);
  }


  @Bean
  public ItemProcessor<Article, Article> articleProcessor() {
    //중복 검사
    return article ->
        articleRepository.findByTitleAndDate(article.getTitle(), article.getDate()) != null ? null : article;
  }
  @Bean
  public RepositoryItemWriter<Article> articleWriterWithDB() {

    return new RepositoryItemWriterBuilder<Article>().repository(articleRepository)
        .methodName("save").build();
  }

  @Bean
  public ItemWriter<Article> articleWriterWithDBAndFileSync() {
    return chunk -> {
      List<? extends Article> articles = chunk.getItems();
      if (articles.isEmpty()) return;

      String today = LocalDate.now().toString();
      File file = new File("articles_" + today + ".json");

      Set<String> existingKeys = new HashSet<>();
      JSONArray jsonArray = new JSONArray();

      // 1️⃣ 기존 JSON 파일 읽기
      if (file.exists()) {
        String content = Files.readString(file.toPath());
        //System.out.println("content = " + content);
        jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject obj = jsonArray.getJSONObject(i);
          String key = obj.getString("title") + "|" + obj.getString("date");
          existingKeys.add(key);
        }
      }

      // 2️⃣ DB에 저장할 기사 및 JSON에 추가할 기사 추리기
      List<Article> newArticlesForDB = new LinkedList<>();
      for (Article article : articles) {
        String key = article.getTitle() + "|" + article.getDate().toString();
        if (!existingKeys.contains(key)) {
          // JSON에 추가
          JSONObject json = new JSONObject();
          json.put("id", article.getId());
          json.put("title", article.getTitle());
          json.put("summary", article.getSummary());
          json.put("link", article.getLink());
          json.put("source", article.getSource());
          json.put("date", article.getDate().toString());
          json.put("commentCount", article.getCommentCount());
          jsonArray.put(json);

          // DB 저장용
          newArticlesForDB.add(article);
          existingKeys.add(key);
        }
      }

      // 3️⃣ 파일 덮어쓰기
      try (FileWriter writer = new FileWriter(file)) {
        writer.write(jsonArray.toString(2));
      }

      // 4️⃣ DB 저장
      List<Article> fullSyncList = new LinkedList<>();
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject obj = jsonArray.getJSONObject(i);
        Article article = new Article(
            new ArticleBaseDto(
                null,
                obj.getString("title"),
                obj.getString("summary"),
                obj.getString("link"),
                obj.getString("source"),
                LocalDateTime.parse(obj.getString("date")),
                null,
                obj.getInt("commentCount"),
                null
            )
        );
        fullSyncList.add(article);
      }
      for (Article article : fullSyncList) {
        if(articleRepository.findByTitleAndDate(article.getTitle(), article.getDate()) == null) {
          articleService.save(new ArticleBaseDto(article));
        }
      }
      //articleRepository.saveAll(newArticlesForDB);

      System.out.println("✅ 저장 완료: DB " + newArticlesForDB.size() + "건, JSON 누적 " + jsonArray.length() + "건");
    };
  }



}
