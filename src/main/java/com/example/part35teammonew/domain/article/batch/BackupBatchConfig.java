package com.example.part35teammonew.domain.article.batch;

import com.example.part35teammonew.domain.article.dto.ArticleBaseDto;
import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;
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
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BackupBatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;
  private final ArticleRepository articleRepository;
  private final S3UploadArticle s3UploadArticle;


  @Bean
  public Job backupJob() {
    return new JobBuilder("backupJob", jobRepository).start(backupStep()).build();
  }

  @Bean
  public Step backupStep() {
    System.out.println("BackupBatchConfig");
    //10개씩 끊어서 처리
    return new StepBuilder("backupStep", jobRepository).<Article, Article>chunk(10, platformTransactionManager)
        .reader(articleBackupReader(null, null))
        .processor(articleBackupProcessor())
        .writer(articleBackupWriterWithDB())
        .build();
  }

  @Bean
  @StepScope
  public ItemReader<Article> articleBackupReader(
      @Value("#{jobParameters['from']}") String from,
      @Value("#{jobParameters['to']}") String to
  ) {
    return new ItemReader<>() {
      private Queue<Article> queue = new LinkedList<>();
      private boolean loaded = false;

      @Override
      public Article read() throws Exception {
        if (!loaded) {
          loaded = true;
          try {
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);
            //String today = LocalDate.now().toString();
            for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)){
              File file = new File("articles_" + date + "temp.json");
              if (s3UploadArticle.exists("articles_" + date + ".json")) {
                s3UploadArticle.download(file);
                String content = Files.readString(file.toPath());
                JSONArray jsonArray = new JSONArray(content);

                for (int i = 0; i < jsonArray.length(); i++) {
                  JSONObject obj = jsonArray.getJSONObject(i);
                  Article article = new Article(new ArticleBaseDto(
                      obj.getString("title"),
                      obj.getString("summary"),
                      obj.getString("link"),
                      obj.getString("source"),
                      LocalDateTime.parse(obj.getString("date")),
                      obj.optInt("commentCount", 0) // commentCount 없을 수도 있어서 optInt
                  ));
                  queue.add(article);
                }
              }
              if (!file.delete()) {
                System.err.println("⚠️ 파일 삭제 실패: " + file.getAbsolutePath());
              }
            }
            System.out.println("Loaded articles: " + queue.size());
          } catch (Exception e) {
            throw new RuntimeException("S3 JSON 파일 로딩 실패", e);
          }

          /*  File file = new File("articles_" + today + "temp.json");
            JSONArray jsonArray = new JSONArray();

            if (s3UploadArticle.exists("articles_" + today + ".json")) {
              s3UploadArticle.download(file);
              String content = Files.readString(file.toPath());
              jsonArray = new JSONArray(content);
            } else {
              throw new IllegalArgumentException("S3 Bucket에 파일이 존재하지 않습니다.");
            }

            for (int i = 0; i < jsonArray.length(); i++) {
              JSONObject obj = jsonArray.getJSONObject(i);
              Article article = new Article(new ArticleBaseDto(
                  obj.getString("title"),
                  obj.getString("summary"),
                  obj.getString("link"),
                  obj.getString("source"),
                  LocalDateTime.parse(obj.getString("date")),
                  obj.getInt("commentCount")
              ));
              queue.add(article);
            }
            System.out.println("articleBackupReader_queue = " + queue.size());

            // 파일 삭제
            if (!file.delete()) {
              System.err.println("⚠️ 파일 삭제 실패: " + file.getAbsolutePath());
            }
          } catch (Exception e) {
            throw new RuntimeException("S3 JSON 파일 로딩 실패", e);
          }*/
        }
        return queue.poll();
      }
    };
  }

  @Bean
  public ItemProcessor<Article, Article> articleBackupProcessor() {
    //중복 검사
    return article ->
        articleRepository.findByTitleAndDate(article.getTitle(), article.getDate()) != null ? null : article;
  }

  @Bean
  public RepositoryItemWriter<Article> articleBackupWriterWithDB() {
    return new RepositoryItemWriterBuilder<Article>().repository(articleRepository)
        .methodName("save").build();
  }
}
