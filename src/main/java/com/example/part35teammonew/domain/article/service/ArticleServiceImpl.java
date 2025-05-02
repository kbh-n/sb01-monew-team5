package com.example.part35teammonew.domain.article.service;

import com.example.part35teammonew.domain.article.batch.S3UploadArticle;
import com.example.part35teammonew.domain.article.dto.ArticleBaseDto;
import com.example.part35teammonew.domain.article.dto.ArticlesResponse;
import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.entity.Direction;
import com.example.part35teammonew.domain.article.entity.SortField;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import com.example.part35teammonew.domain.articleView.Dto.ArticleViewDto;
import com.example.part35teammonew.domain.articleView.service.ArticleViewServiceInterface;
import com.example.part35teammonew.domain.interest.service.InterestService;
import com.example.part35teammonew.domain.interestUserList.service.InterestUserListServiceInterface;
import com.example.part35teammonew.domain.notification.service.NotificationServiceInterface;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

  private final ArticleRepository articleRepository;
  private final ArticleViewServiceInterface articleViewServiceInterface;
  private final S3UploadArticle s3UploadArticle;
  private final NotificationServiceInterface notificationServiceInterface;
  private final InterestService interestService;
  private final InterestUserListServiceInterface interestUserListServiceInterface;

  // 기사 저장
  @Override
  public UUID save(ArticleBaseDto dto) {
    if (dto.getTitle() == null || dto.getTitle().isBlank() || dto.getPublishDate() == null) {
      throw new IllegalArgumentException("제목과 날짜는 필수입니다.");
    }
    if (articleRepository.findByTitleAndDate(dto.getTitle(), dto.getPublishDate()) != null) {
      throw new IllegalArgumentException("중복 저장되었습니다.");
    }

    Article article = new Article(dto);
    Article saved = articleRepository.save(article);//저장

    ArticleViewDto articleViewDto = articleViewServiceInterface.createArticleView(
        saved.getId());//뷰테이블 만듬

    //관심사, 키워드 추출
    String articleTitle = article.getTitle();
    UUID articleId = article.getId();
    List<Pair<String, UUID>> getInterest = interestService.getInterestList();
    Set<UUID> containedId = new HashSet<>();
    Set<UUID> targetUserID = new HashSet<>();//Set<UUID> 유저아이디: 구독중인 유저들

    //title.contains()//
    for (Pair<String, UUID> pair : getInterest) {
      if (pair.getLeft().contains(articleTitle)) {
        containedId.add(pair.getRight());//Set<UUID> 관심사id 들 : 관심사 x 제목 x 키워드로 거른
        saved.setInterestId(pair.getRight());//기사의 관심사 설정
      }
    }

    //관심사, 키워드를 구독중인 유저 얼아내기
    for (UUID interestId : containedId) {
      List<UUID> findUser = interestUserListServiceInterface.getAllUserNowSubscribe(interestId);
      targetUserID.addAll(findUser);
    }

    //찾은 유저에게 알람보내기
    for (UUID userId : targetUserID) {
      notificationServiceInterface.addNewsNotice(userId, "관심있는 뉴스 등록", articleId);
    }

    return saved.getId();
  }

  // Id로 기사 단건 조회
  @Override
  public ArticleBaseDto findById(UUID id) {
    return articleRepository.findById(id)
        .filter(Article::isNotLogicallyDeleted)
        .map(ArticleBaseDto::new)
        .orElseThrow(() -> new IllegalArgumentException("해당 ID의 기사를 찾을 수 없습니다."));
  }

  @Override
  public List<ArticleBaseDto> findAll() {
    List<ArticleBaseDto> articles = new ArrayList<>();
    articleRepository.findAll().stream().filter(Article::isNotLogicallyDeleted)
        .map(ArticleBaseDto::new).forEach(articles::add);
    return articles;
  }

  @Override
  public List<ArticleBaseDto> findByIds(List<UUID> ids) {
    List<ArticleBaseDto> articles = new ArrayList<>();
    for (UUID id : ids) {
      articles.add(findById(id));
    }
    return articles;
  }

  // 기사 삭제
  @Override
  public void deletePhysical(UUID id) {
    if (articleRepository.findById(id).isPresent()) {
      articleRepository.deleteById(id);
      return;
    }
    throw new IllegalArgumentException("해당 ID의 기사를 찾을 수 없습니다.");
  }

  @Override
  public void deleteLogical(UUID id) {
    //deletedAt 존재하며 현재 시간보다 이전이면 제거된 것으로 침
    //다른 검색 메서드에서도 포함시켜야할 듯
    Optional<Article> article = articleRepository.findById(id);
    if (article.isPresent()) {
      article.get().logicalDelete(LocalDateTime.now());
    } else {
      throw new IllegalArgumentException("해당 ID의 기사를 찾을 수 없습니다.");
    }
  }


  @Override
  public List<UUID> backup(String from, String to) {
    LocalDate localStartDate = LocalDate.parse(from.substring(0, 10),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    LocalDate localEndDate = LocalDate.parse(to.substring(0, 10),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    List<UUID> result = new ArrayList<>();
    Queue<Article> queue = new LinkedList<>();
    while (localStartDate.isBefore(localEndDate)) {
      File file = new File("articles_" + localStartDate + "temp.json");
      JSONArray jsonArray;
      try {
        if (s3UploadArticle.exists("articles_" + localStartDate + ".json")) {
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
      } catch (Exception e) {

      }
      while (!queue.isEmpty()) {
        Article article = queue.poll();
        article =
            articleRepository.findByTitleAndDate(article.getTitle(), article.getDate()) != null
                ? null : article;
        if (article != null) {
          save(new ArticleBaseDto(article));
          result.add(article.getId());
          System.out.println("article = " + article);
        }
      }
    }
    return result;
  }

  @Override
  public void increaseCountReadUser(UUID id){
    Article article=articleRepository.findById(id).
        orElseThrow(() -> new NoSuchElementException("기사 없음"));
    article.increaseReadCount();
  }

  @Override
  public ArticlesResponse getPageArticle(String keyword, String interestId, String[] sourceIn,
      String publishDateFrom, String publishDateTo, String orderBy, String direction, String cursor,
      String after, int limit, String userId) {

    Sort sort = switch (SortField.valueOf(orderBy)) {
      case publishDate -> Sort.by(Sort.Direction.fromString(direction), "date");
      case commentCount -> Sort.by(Sort.Direction.fromString(direction), "commentCount");
      case viewCount -> Sort.by(Sort.Direction.fromString(direction), "viewCount");
    };
    System.out.println("sort = " + sort);

    int page = 0;
    if (cursor != null && !cursor.isBlank()) {
      try {
        page = Integer.parseInt(cursor);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("커서는 숫자여야 합니다.");
      }
    }

    Pageable pageable = PageRequest.of(page, limit, sort);

    UUID interestUUID = null;
    if (interestId != null && !interestId.isBlank()) {
      interestUUID = UUID.fromString(interestId);
    }

    LocalDateTime from = null;
    if (publishDateFrom != null && !publishDateFrom.isBlank()) {
      from = LocalDateTime.parse(publishDateFrom);
    }

    LocalDateTime to = null;
    if (publishDateTo != null && !publishDateTo.isBlank()) {
      to = LocalDateTime.parse(publishDateTo);
    }

    List<String> sources = (sourceIn != null && sourceIn.length > 0) ? Arrays.asList(sourceIn) : null;

    keyword = "인천";
    keyword = "%"+keyword+"%";


    Page<Article> result;
    if (sources != null) {
      result = articleRepository.searchArticlesWithSources(
          keyword, interestUUID, from, to, sources, pageable
      );
    } else if( keyword != null ) {
      result = articleRepository.searchArticlesWithoutAll( keyword, pageable );
      System.out.println("result.getSize() = " + result.getSize());
      System.out.println("result.getContent() = " + result.getContent());
    }else {
      result = articleRepository.searchArticlesWithoutSources(
          keyword, interestUUID, from, to, pageable
      );
    }

    List<ArticleBaseDto> content = result.getContent().stream()
        .map(ArticleBaseDto::new)
        .toList();

    String nextAfter = null;
    if (!content.isEmpty() && orderBy.equals(SortField.publishDate.toString())) {
      nextAfter = content.get(content.size() - 1).getPublishDate().toString();
    }

    ArticlesResponse response = new ArticlesResponse();
    response.setContent(content);
    response.setSize(content.size());
    response.setTotalElements((int) result.getTotalElements());
    response.setHasNext(String.valueOf(result.hasNext()));
    response.setNextCursor(String.valueOf(page + 1));
    response.setNextAfter(nextAfter);

    return response;
  }


}
