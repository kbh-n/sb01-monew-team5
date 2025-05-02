package com.example.part35teammonew.domain.article.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.example.part35teammonew.domain.article.dto.ArticleBaseDto;
import com.example.part35teammonew.domain.article.service.ArticleService;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jdk.jfr.Name;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ArticleScheduleTest {
  @Autowired
  private ArticleSchedule articleSchedule;
  @Autowired
  private S3UploadArticle s3UploadArticle;
  @Autowired
  private ArticleService articleService;

  @Test
  void testS3AndDBAndLocalConsistency() throws Exception {
    // Step 1, 2: DB와 S3 백업 작업 실행
    System.out.println("RUN ARTICLE JOB");
    articleSchedule.runArticleJob(); // DB 저장
    Thread.sleep(5000);
    System.out.println("RUN S3JOB");
    articleSchedule.runS3Job(); // S3 저장

    // Step 3: 오늘 날짜 파일명 생성
    String today = LocalDate.now().toString();
    String filename = "articles_" + today + ".json";
    File localFile = new File(filename);

    // Step 4: S3 파일에서 제목 수집
    Set<String> s3Titles = new HashSet<>();
    if (s3UploadArticle.exists(filename)) {
      s3UploadArticle.download(localFile); // 덮어쓰기
      String content = Files.readString(localFile.toPath());
      JSONArray jsonArray = new JSONArray(content);
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject obj = jsonArray.getJSONObject(i);
        s3Titles.add(obj.getString("title"));
      }
    }

    // Step 5: DB에서 제목 수집
    Set<String> dbTitles = new HashSet<>();
    for (ArticleBaseDto articleBaseDto : articleService.findAll()) {
      dbTitles.add(articleBaseDto.getTitle());
    }

    // Step 6: 로컬 파일에서 제목 수집
    Set<String> localTitles = new HashSet<>();
    if (localFile.exists()) {
      String localContent = Files.readString(localFile.toPath());
      JSONArray localJson = new JSONArray(localContent);
      for (int i = 0; i < localJson.length(); i++) {
        JSONObject obj = localJson.getJSONObject(i);
        localTitles.add(obj.getString("title"));
      }
    }

    // Step 7: 개수 출력 및 비교
    System.out.println("🔎 DB 개수:     " + dbTitles.size());
    System.out.println("🔎 S3 개수:     " + s3Titles.size());
    System.out.println("🔎 로컬 파일 개수: " + localTitles.size());
    int dbSize = dbTitles.size();
    int s3Size = s3Titles.size();
    int localSize = localTitles.size();

    assertThat(Math.abs(dbSize - s3Size))
        .withFailMessage("DB와 S3 기사 수 차이가 15 이상입니다: DB=%d, S3=%d", dbSize, s3Size)
        .isLessThan(dbSize/10);

    assertThat(Math.abs(s3Size - localSize))
        .withFailMessage("S3와 로컬 기사 수 차이가 15 이상입니다: S3=%d, Local=%d", s3Size, localSize)
        .isLessThan(s3Size/10);

  }

  @Test
  void testRestoreFromBackup() throws Exception {
    // 미리 실행
    articleSchedule.runArticleJob();
    articleSchedule.runS3Job();

    String today = LocalDate.now().toString();
    File file = new File("articles_" + today + ".json");

    if (s3UploadArticle.exists(file.getName())) {
      s3UploadArticle.download(file);
      String content = Files.readString(file.toPath());
      JSONArray jsonArray = new JSONArray(content);

      // Step 6: DB 삭제 (절반만 삭제)
      List<ArticleBaseDto> all = articleService.findAll();
      all = all.subList(0, all.size()/2);
      System.out.println("all.size() = " + all.size());
      for (ArticleBaseDto articleBaseDto : all) {
        articleService.deletePhysical(articleBaseDto.getId());
      }


      // Step 7: 복구 스케줄 실행
      articleSchedule.runBackupJob();

      // Step 8: 복구 후 다시 비교
      int restoredCount = articleService.findAll().size();
      int boundary = jsonArray.length() / 10;
      assertThat(Math.abs(jsonArray.length() - restoredCount)).withFailMessage("복구 전 후 DB의 수의 차가 15 이하여야 합니다.").isLessThan(boundary);
    } else {
      fail("S3에 해당 JSON 파일 없음. 백업 실행 전인지 확인할 것.");
    }
  }

  @Test
  @Name("S3와 DB의 기사 수 차이")
  void compareS3AndDBArticles() throws Exception {
    System.out.println("RUN ARTICLE JOB");
    articleSchedule.runArticleJob(); //db

    String today = LocalDate.now().toString();
    File file = new File("articles_" + today + ".json");

    // S3에서 다운로드
    if (!s3UploadArticle.exists(file.getName())) {
      fail("S3 파일 없음: " + file.getName());
    }
    s3UploadArticle.download(file);

    // S3 JSON 파싱
    String content = Files.readString(file.toPath());
    JSONArray jsonArray = new JSONArray(content);

    // S3에서 얻은 고유 article key (title + date)
    Set<String> s3Keys = new HashSet<>();
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject obj = jsonArray.getJSONObject(i);
      String title = obj.getString("title");
      String date = obj.getString("date");
      s3Keys.add(title + "::" + date);
    }

    // DB에서 얻은 고유 article key
    Set<String> dbKeys = new HashSet<>();
    for (ArticleBaseDto article : articleService.findAll()) {
      dbKeys.add(article.getTitle() + "::" + article.getDate().toString());
    }

    // 🔍 S3엔 있는데 DB엔 없는 것
    Set<String> s3Only = new HashSet<>(s3Keys);
    s3Only.removeAll(dbKeys);

    // 🔍 DB엔 있는데 S3엔 없는 것
    Set<String> dbOnly = new HashSet<>(dbKeys);
    dbOnly.removeAll(s3Keys);

    System.out.println("✅ S3 총 개수: " + s3Keys.size());
    System.out.println("✅ DB 총 개수: " + dbKeys.size());
    System.out.println("❗ DB에 없는 S3 기사: " + s3Only.size());
    s3Only.forEach(s -> System.out.println("   - " + s));
    System.out.println("❗ S3에 없는 DB 기사: " + dbOnly.size());
    dbOnly.forEach(s -> System.out.println("   - " + s));

    int boundary_s3 = s3Keys.size() / 10;
    int boundary_db = dbKeys.size() / 10;

    assertThat(s3Only.size())
        .withFailMessage("DB에 누락된 S3 기사가 15 이하여야 함 (누락 수: %d)", s3Only.size())
        .isLessThan(boundary_s3);

    assertThat(dbOnly.size())
        .withFailMessage("S3에 누락된 DB 기사가 15 이하여야 함 (누락 수: %d)", dbOnly.size())
        .isLessThan(boundary_db);

    // 파일 삭제
    file.delete();
  }

}