package com.example.part35teammonew.domain.article.controller;

import com.example.part35teammonew.domain.article.dto.ArticleBaseDto;
import com.example.part35teammonew.domain.article.dto.ArticleCursorRequest;
import com.example.part35teammonew.domain.article.dto.ArticleEnrollmentResponse;
import com.example.part35teammonew.domain.article.dto.ArticleSourceAndDateAndInterestsRequest;
import com.example.part35teammonew.domain.article.dto.ArticlesResponse;
import com.example.part35teammonew.domain.article.dto.findByCursorPagingResponse;
import com.example.part35teammonew.domain.article.entity.Direction;
import com.example.part35teammonew.domain.article.entity.SortField;
import com.example.part35teammonew.domain.article.service.ArticleService;
import com.example.part35teammonew.domain.articleView.service.ArticleViewServiceInterface;
import com.example.part35teammonew.domain.comment.dto.CommentPageResponse;
import com.example.part35teammonew.domain.comment.service.CommentService;
import com.example.part35teammonew.domain.userActivity.maper.ArticleInfoViewMapper;
import com.example.part35teammonew.domain.userActivity.service.UserActivityServiceInterface;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ArticleController {

  private final ArticleService articleService;
  private final JobLauncher jobLauncher;
  private final Job backupJob;
  private final ArticleViewServiceInterface articleViewServiceInterface;
  private final UserActivityServiceInterface userActivityServiceInterface;
  private final ArticleInfoViewMapper articleInfoViewMapper;
  private final CommentService commentService;


  @PostMapping("/api/articles/{articleId}/article-views")
  public ResponseEntity<ArticleEnrollmentResponse> articleViewEnrollment(
      @PathVariable UUID articleId, @RequestHeader("Monew-Request-User-ID") String userId) {
    ArticleEnrollmentResponse articleEnrollmentResponse = new ArticleEnrollmentResponse();
    ArticleBaseDto articleBaseDto = articleService.findById(articleId);

    UUID requestUserId = null;
    try {
      requestUserId = UUID.fromString(userId);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 사용자 ID 형식입니다");
    }

    if(articleViewServiceInterface.addReadUser(articleId, requestUserId)){
      articleService.increaseCountReadUser(articleId);
    }

    articleEnrollmentResponse.setId(articleId);
    articleEnrollmentResponse.setViewedBy(requestUserId);
    articleEnrollmentResponse.setCreatedAt(articleBaseDto.getCreatedAt().toLocalDate());
    articleEnrollmentResponse.setArticleId(articleId);
    articleEnrollmentResponse.setSource(articleBaseDto.getSource());
    articleEnrollmentResponse.setSourceUrl(articleBaseDto.getSourceUrl());
    articleEnrollmentResponse.setArticleTitle(articleBaseDto.getTitle());
    articleEnrollmentResponse.setArticlePublishedDate(articleBaseDto.getPublishDate());
    articleEnrollmentResponse.setArticleSummary(articleBaseDto.getSummary());

    CommentPageResponse comments = commentService.getComments(articleId, null, null, null, null, null, null);
    articleEnrollmentResponse.setArticleCommentCount(comments.getSize());
    articleEnrollmentResponse.setArticleViewCount(articleBaseDto.getViewCount());

    userActivityServiceInterface.addArticleInfoView(requestUserId, articleInfoViewMapper.toDto(articleEnrollmentResponse, requestUserId));

    return ResponseEntity.ok(articleEnrollmentResponse);
  }

  @GetMapping("/api/articles")
  public ResponseEntity<ArticlesResponse> articles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String interestId,
      @RequestParam(required = false) String[] sourceIn,
      @RequestParam(required = false) String publishDateFrom,
      @RequestParam(required = false) String publishDateTo,
      @RequestParam String orderBy, //코맨트수, 조회수, 시간순
      @RequestParam String direction,
      @RequestParam(required = false) String cursor, //orderby에 따라 달라짐
      @RequestParam (required = false) String after, //시간
      @RequestParam int limit,
      @RequestHeader("Monew-Request-User-ID") String userId
  ) {
    if ( limit == 0 ){
      //페이지 맨 아래로 내려가고 휠을 움직이면 에러 발생
      //프런트 문제일수도?
      return ResponseEntity.badRequest().body(null);
    }
    //같은게 있으면 제목순
    ArticlesResponse result = articleService.getPageArticle(keyword, interestId,
        sourceIn, publishDateFrom, publishDateTo, orderBy, direction, cursor, after, limit, userId);

    return ResponseEntity.ok(result);

  }

  @GetMapping("/api/articles/restore")
  public ResponseEntity<ArticleEnrollmentResponse> articlesRestore(
      @RequestParam("from") String from,
      @RequestParam("to") String to) throws Exception {

    JobParameters jobParameters = new JobParametersBuilder()
        .addString("from", from)
        .addString("to", to)
        .addLong("time", System.currentTimeMillis()) // 중복방지용
        .toJobParameters();

    jobLauncher.run(backupJob, jobParameters);

    return ResponseEntity.ok(null);
  }

  @DeleteMapping("/api/articles/{articleId}")
  public ResponseEntity<Void> articlesDelete(@PathVariable UUID articleId) {
    articleService.deleteLogical(articleId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/api/articles/{articleId}/hard")
  public ResponseEntity<Void> articlesDeleteHard(@PathVariable UUID articleId) {
    articleService.deletePhysical(articleId);
    return ResponseEntity.noContent().build();
  }
}