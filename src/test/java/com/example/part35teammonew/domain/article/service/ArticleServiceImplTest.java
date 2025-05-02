package com.example.part35teammonew.domain.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.part35teammonew.domain.article.dto.ArticleBaseDto;
import com.example.part35teammonew.domain.article.dto.ArticleCursorRequest;
import com.example.part35teammonew.domain.article.dto.ArticleSourceAndDateAndInterestsRequest;
import com.example.part35teammonew.domain.article.dto.findByCursorPagingResponse;
import com.example.part35teammonew.domain.article.entity.Direction;
import com.example.part35teammonew.domain.article.entity.SortField;
import com.example.part35teammonew.domain.articleView.service.ArticleViewServiceInterface;
import com.example.part35teammonew.domain.interest.entity.Interest;
import com.example.part35teammonew.domain.interest.repository.InterestRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import jdk.jfr.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ArticleServiceImplTest {

  @Autowired
  private ArticleServiceImpl articleServiceImpl;
  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private ArticleViewServiceInterface articleViewService;

  private ArticleBaseDto createArticleBaseDto(String title) {
    ArticleBaseDto dto = new ArticleBaseDto(title, "summary", "link", "source",
        LocalDateTime.now(), 0);
    return dto;
  }

  private ArticleBaseDto createArticleBaseDto(String title, String summary) {
    ArticleBaseDto dto = new ArticleBaseDto(title, summary, "link", "source", LocalDateTime.now(), 0);
    return dto;
  }

  private ArticleBaseDto createArticleBaseDto(String title, String summary, String[] sources,
      LocalDateTime dateTime) {
    ArticleBaseDto dto = new ArticleBaseDto(title, summary, "link", sources[0], dateTime, 0);
    return dto;
  }

  @Test
  @Name("기사 생성")
  void saveArticle() {
    ArticleBaseDto hello = createArticleBaseDto("hello");
    //
    UUID savedId = articleServiceImpl.save(hello);
    ArticleBaseDto articleBaseDto = articleServiceImpl.findById(savedId);
    //
    assertThat(savedId).isEqualTo(articleBaseDto.getId());
  }

  @Test
  @Name("기사 중복 생성")
  void DuplicatedArticle() {
    ArticleBaseDto hello = createArticleBaseDto("hello");
    UUID savedId = articleServiceImpl.save(hello);

    assertThatThrownBy(() -> articleServiceImpl.save(hello)).isInstanceOf(
            IllegalArgumentException.class) // 예외 타입
        .hasMessageContaining("중복 저장되었습니다."); // 예외 메시지 확인
  }

  @Test
  @Name("기사 삭제")
  void deleteArticle() {
    ArticleBaseDto hello = createArticleBaseDto("hello");
    UUID savedId = articleServiceImpl.save(hello);
    //
    articleServiceImpl.deletePhysical(savedId);
    //
    assertThatThrownBy(() -> articleServiceImpl.findById(savedId)).isInstanceOf(
            IllegalArgumentException.class) // 예외 타입
        .hasMessageContaining("해당 ID의 기사를 찾을 수 없습니다."); // 예외 메시지 확인
  }

  @Test
  @Name("기사가 없는데 삭제하려고 함")
  void deleteNotExistArticle() {
    assertThatThrownBy(() -> articleServiceImpl.deletePhysical(UUID.randomUUID())).isInstanceOf(
            IllegalArgumentException.class) // 예외 타입
        .hasMessageContaining("해당 ID의 기사를 찾을 수 없습니다."); // 예외 메시지 확인
  }

  @Test
  @Name("기사 목록 테스트")
  void savedArticles() {
    ArticleBaseDto hello = createArticleBaseDto("hello");
    ArticleBaseDto hello2 = createArticleBaseDto("hello2");
    ArticleBaseDto hello3 = createArticleBaseDto("hello3");
    //
    UUID savedId = articleServiceImpl.save(hello);
    UUID savedId2 = articleServiceImpl.save(hello2);
    UUID savedId3 = articleServiceImpl.save(hello3);
    //
    assertThat(articleServiceImpl.findAll().size()).isEqualTo(3);
  }

  @Test
  @Name("제목과 요약 내용으로 기사 찾기")
  void findByTitleOrSummary() {
    //참고 : 타이틀과 기사 작성 시간이 같으면 중복
    ArticleBaseDto hello = createArticleBaseDto("hello1", "summary1");
    ArticleBaseDto hello2 = createArticleBaseDto("hello2", "summary1");
    ArticleBaseDto hello3 = createArticleBaseDto("hello3", "summary1");
    ArticleBaseDto hello4 = createArticleBaseDto("hello4", "summary1");
    ArticleBaseDto hello5 = createArticleBaseDto("hello5", "summary1");
    //
    articleServiceImpl.save(hello);
    articleServiceImpl.save(hello2);
    articleServiceImpl.save(hello3);
    articleServiceImpl.save(hello4);
    articleServiceImpl.save(hello5);
    //
    assertThat(articleServiceImpl.findByTitleOrSummary("ello", "summary").size()).isEqualTo(5);
    assertThat(articleServiceImpl.findByTitleOrSummary("hello5", "summary1").size()).isEqualTo(5);
    assertThat(articleServiceImpl.findByTitleOrSummary(null, "summary1").size()).isEqualTo(5);
    assertThat(articleServiceImpl.findByTitleOrSummary("ello4", null).size()).isEqualTo(1);
  }

  @Test
  @Name("작성 시간, 소스, 관심사로 기사 찾기")
  void findBySourceAndDateAndInterests() {
    String[] sources = {"source1"};
    ArticleBaseDto hello1 = createArticleBaseDto("Spring과 Java를 활용한 REST API 개발 실무 가이드", "summary1",sources, LocalDateTime.now());
    ArticleBaseDto hello2 = createArticleBaseDto("Java 개발자를 위한 최신 Spring Security 가이드", "summary1",sources, LocalDateTime.now());
    ArticleBaseDto hello3 = createArticleBaseDto("Spring 기반 마이크로서비스 아키텍처 개요", "summary1", sources, LocalDateTime.now());
    ArticleBaseDto python1 = createArticleBaseDto("AI 모델 개발을 위한 파이썬 기초 튜토리얼", "summary1", sources, LocalDateTime.now());
    ArticleBaseDto python2 = createArticleBaseDto("Python 입문자를 위한 머신러닝 시작 가이드", "summary1",sources, LocalDateTime.now());
    ArticleBaseDto python3 = createArticleBaseDto("AI 시대, 파이썬이 여전히 인기인 이유", "summary1", sources,LocalDateTime.now());
    ArticleBaseDto boot1 = createArticleBaseDto("Spring Boot로 빠르게 만드는 웹 애플리케이션 가이드", "summary1",sources, LocalDateTime.now());
    ArticleBaseDto boot2 = createArticleBaseDto("Spring Boot 3.x 업그레이드 체크리스트", "summary1",sources, LocalDateTime.now());
    ArticleBaseDto boot3 = createArticleBaseDto("부트캠프 출신 개발자의 Spring Boot 실전 후기", "summary1",sources, LocalDateTime.now());

    Interest i1 = new Interest();
    i1.setName("Java Spring");
    i1.setKeywords("java,spring");
    interestRepository.save(i1);

    Interest i2 = new Interest();
    i2.setName("Python Basics");
    i2.setKeywords("python,AI");
    interestRepository.save(i2);

    Interest i3 = new Interest();
    i3.setName("Spring Boot");
    i3.setKeywords("boot,spring");
    interestRepository.save(i3);
    //
    articleServiceImpl.save(hello1);    articleServiceImpl.save(hello2);    articleServiceImpl.save(hello3);
    articleServiceImpl.save(python1);    articleServiceImpl.save(python2);    articleServiceImpl.save(python3);
    articleServiceImpl.save(boot1);    articleServiceImpl.save(boot2);    articleServiceImpl.save(boot3);
    //
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, null, null, new String[]{"source1"}))
        .size()).isEqualTo(9);
    assertThat(
        articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, LocalDate.now().toString(),null, null)).size()).isEqualTo(9);
    assertThat(
        articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, LocalDate.now().toString(), null, new String[]{"source1"})).size()).isEqualTo(9);
    assertThat(
        articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest("java",null,LocalDate.now().toString(),null)).size()).isEqualTo(2);
    assertThat(
        articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest("java",LocalDate.now().toString(), null, null)).size()).isEqualTo(2);
    assertThat(
        articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest("가이드",null,LocalDate.now().toString(),null )).size()).isEqualTo(4);

    // ✅ 예외 케이스
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, null, null, null)))
        .isEqualTo(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, LocalDateTime.now().toString(), null, null)));
  }

  @Test
  @Name("논리적 삭제된 기사는 조회되지 않아야 한다")
  void logicallyDeletedArticleShouldNotBeFound() {
    // given
    ArticleBaseDto article = createArticleBaseDto("삭제될 기사");
    UUID savedId = articleServiceImpl.save(article);

    // when: 논리적 삭제 수행
    articleServiceImpl.deleteLogical(savedId);

    // then: findById 시 예외 발생해야 함
    assertThatThrownBy(() -> articleServiceImpl.findById(savedId)).isInstanceOf(
        IllegalArgumentException.class).hasMessageContaining("해당 ID의 기사를 찾을 수 없습니다.");

    // and: findAll 결과에도 포함되지 않아야 함
    List<ArticleBaseDto> articles = articleServiceImpl.findAll();
    assertThat(articles.stream().noneMatch(a -> savedId.equals(a.getId()))).isTrue();
  }

  @Test
  @Name("source, date, interests 조합에 따른 기사 조회 테스트 (논리삭제 포함)")
  void findBySourceDateInterests_withLogicalDeleteCheck() {
    String[] source = {"source1"};
    LocalDate now = LocalDate.now();
    LocalDate yesterday = now.minusDays(1);

    String startDate = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String endDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    ArticleBaseDto article1 = createArticleBaseDto("Spring Boot Guide", "내용1", source, now.atTime(11,50));
    ArticleBaseDto article2 = createArticleBaseDto("Python ML Intro", "내용2", source, now.atTime(11,50));
    ArticleBaseDto article3 = createArticleBaseDto("No match title", "내용3", source, now.atTime(11,50));

    UUID id1 = articleServiceImpl.save(article1);
    UUID id2 = articleServiceImpl.save(article2);
    UUID id3 = articleServiceImpl.save(article3);

    // when: 일부 기사 논리적 삭제
    articleServiceImpl.deleteLogical(id3);

    // then: source + date만 검색
    List<ArticleBaseDto> result1 = articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null,
        yesterday.toString(),null, source));
    assertThat(result1).hasSize(2); // 논리삭제된 1건 제외

    // then: interests로 필터링
    List<ArticleBaseDto> result2 = articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest("spring",
        yesterday.toString(), null,source));
    assertThat(result2).hasSize(1);
    assertThat(result2.get(0).getTitle()).contains("Spring");

    // then: 삭제된 기사 필터링 확인
    List<ArticleBaseDto> result3 = articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest("No Match",
        yesterday.toString(),null, source));
    assertThat(result3).isEmpty(); // 논리 삭제된 기사여서 필터링 됨

    // then: 예외 테스트
    // ✅ 예외 케이스
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, null, null, null)))
        .isEqualTo(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, LocalDateTime.now().toString(), null, null)));

  }

  @Test
  @Name("source, date에 따른 기사 조회 테스트 (논리삭제 포함)")
  void findBySourceDateInterests_withLogicalDeleteCheck2() {
    String[] source = {"source1"};
    LocalDateTime now = LocalDate.now().atStartOfDay();
    LocalDateTime yesterday = now.minusDays(1);
    LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

    String startDate = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String endDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String tomorrowDate = tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    System.out.println("now = " + now);
    System.out.println("yesterday = " + yesterday);
    System.out.println("tomorrow = " + tomorrow);
    System.out.println("startDate = " + startDate);
    System.out.println("endDate = " + endDate);
    System.out.println("tomorrowDate = " + tomorrowDate);

    ArticleBaseDto article1 = createArticleBaseDto("Spring Boot Guide", "내용1", source, now);
    ArticleBaseDto article2 = createArticleBaseDto("Python ML Intro", "내용2", source, now);
    ArticleBaseDto article3 = createArticleBaseDto("No match title", "내용3", source, now);

    UUID id1 = articleServiceImpl.save(article1);
    UUID id2 = articleServiceImpl.save(article2);
    UUID id3 = articleServiceImpl.save(article3);

    // when: 일부 기사 논리적 삭제
    articleServiceImpl.deleteLogical(id3);

    // then: source + date만 검색
    List<ArticleBaseDto> result1 = articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, startDate,null, null));
    assertThat(result1).hasSize(2); // 논리삭제된 1건 제외

    List<ArticleBaseDto> result2 = articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, null, endDate, null));
    assertThat(result2).hasSize(2); // 논리삭제된 1건 제외

    List<ArticleBaseDto> result3 = articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, startDate, null, source));
    assertThat(result3).hasSize(2); // 논리삭제된 1건 제외

    List<ArticleBaseDto> result4 = articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, null, endDate, source));
    assertThat(result4).hasSize(2); // 논리삭제된 1건 제외

    List<ArticleBaseDto> result5 = articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, startDate, endDate, source));
    assertThat(result5).hasSize(2); // 논리삭제된 1건 제외

  }
  @Test
  @DisplayName("다양한 기사 데이터를 바탕으로 모든 조합을 테스트")
  void testFindBySourceAndDateAndInterests_withRealisticArticles() {
    String[] source = {"thelec"};
    String[] altSource = {"zdnet"};
    String interestKeyword = "ai";
    String noMatchKeyword = "quantum";

    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);
    LocalDate twoDaysAgo = today.minusDays(2);

    // 다양한 날짜, 제목, source로 기사 저장 //10개
    List<ArticleBaseDto> articles = List.of(
        createArticleBaseDto("AI takes over development", "내용", source, twoDaysAgo.atTime(9, 0)),
        createArticleBaseDto("Spring Boot Guide", "내용", source, yesterday.atTime(10, 0)),
        createArticleBaseDto("Python AI Tutorial", "내용", source, yesterday.atTime(11, 0)),
        createArticleBaseDto("Cloud computing trends", "내용", altSource, yesterday.atTime(12, 0)),
        createArticleBaseDto("AI for Healthcare", "내용", source, today.atTime(8, 30)),
        createArticleBaseDto("Cybersecurity update", "내용", source, today.atTime(14, 15)),
        createArticleBaseDto("AI in Automotive", "내용", altSource, today.atTime(13, 0)),
        createArticleBaseDto("Legacy Systems", "내용", source, today.atTime(17, 45)),
        createArticleBaseDto("No match content", "내용", source, today.atTime(20, 0)),
        createArticleBaseDto("Spring Security Intro", "내용", source, today.atTime(22, 0))
    );

    // 저장 후 ID 보관
    List<UUID> articleIds = articles.stream()
        .map(articleServiceImpl::save)
        .toList();

    // 하나는 논리 삭제
    articleServiceImpl.deleteLogical(articleIds.get(8)); // "No match content"

    // 날짜 포맷
    String startDate = twoDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String midDate = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String endDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String tomorrow = today.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    // ✅ source만
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, null, null, source))).hasSize(7); // 논리삭제 제외

    // ✅ startDate만
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, midDate, null, null))).hasSize(8);

    // ✅ endDate만
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, null, endDate, null))).hasSize(9); // today 포함

    // ✅ source + startDate
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, startDate, null, source))).hasSize(7);

    // ✅ source + endDate
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, null, endDate, source))).hasSize(7);

    // ✅ source + start + end
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, midDate, endDate, source))).hasSize(6);

    // ✅ interests (ai) 필터 적용
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests( new ArticleSourceAndDateAndInterestsRequest("ai", startDate, tomorrow, source))).hasSize(3);

    // ✅ interests (no match) → 논리삭제된 데이터는 필터링되어야 함
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(noMatchKeyword, startDate, tomorrow, source))).isEmpty();

    // ✅ 예외 케이스
    assertThat(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, null, null, null)))
        .isEqualTo(articleServiceImpl.findBySourceAndDateAndInterests(new ArticleSourceAndDateAndInterestsRequest(null, LocalDateTime.now().toString(), null, null)));
  }

  @Test
  @DisplayName("커서 기반 페이징 - DATE 및 COMMENT_COUNT 정렬 조건 테스트")
  void testCursorPaginationWithoutViewCount() {
    // Given
    String source = "thelec";
    LocalDateTime baseTime = LocalDateTime.of(2025, 4, 22, 10, 0);

    for(int i=0; i<100; i++){
      ArticleBaseDto articleBaseDto = new ArticleBaseDto(null, "Spring" + i, "내용" + i, "link", source, baseTime.minusHours(i), null, i);
      UUID save = articleServiceImpl.save(articleBaseDto);
      System.out.println("save "+i+" = " + articleBaseDto);
    }

    // When: createdAt 기준 오름차순 페이징
    ArticleCursorRequest reqDate = new ArticleCursorRequest();
    reqDate.setSortField(SortField.publishDate);
    reqDate.setDirection(Direction.DESC);
    reqDate.setCursor(baseTime.minusHours(30).toString()); //2025-04-21-06
    reqDate.setSize(10);

    findByCursorPagingResponse resultDate = articleServiceImpl.findByCursorPaging(reqDate);
    System.out.println("resultDate = " + resultDate);

    // Then
    assertThat(resultDate.getArticles()).hasSize(11);
    assertThat(resultDate.getArticles().get(9).getTitle()).isEqualTo("Spring40");

    // When: commentCount 기준 내림차순 페이징 (cursor = 30)
    ArticleCursorRequest reqComment = new ArticleCursorRequest();
    reqComment.setSortField(SortField.COMMENT_COUNT);
    reqComment.setDirection(Direction.ASC);
    reqComment.setCursor("30");
    reqComment.setSize(10);

    findByCursorPagingResponse resultComment = articleServiceImpl.findByCursorPaging(reqComment);
    System.out.println("resultDate = " + resultDate);

    // Then
    assertThat(resultComment.getArticles()).hasSize(11);
    assertThat(resultComment.getArticles().get(0).getCommentCount()).isEqualTo(31);
    assertThat(resultComment.getArticles().get(1).getCommentCount()).isEqualTo(32);
  }

  //@Test
  //@DisplayName("커서 기반 페이징 - ViewCount 정렬 조건 테스트")
  //void testCursorPaginationWithViewCount()
  /*

  @Test
  @DisplayName("커서 기반 페이징 - ViewCount 정렬 조건 테스트")
  void testCursorPaginationWithViewCount() {
    // Given
    String source = "thelec";
    LocalDateTime baseTime = LocalDateTime.of(2025, 4, 22, 10, 0);

    for(int i=0; i<100; i++){
      ArticleBaseDto articleBaseDto = new ArticleBaseDto(null, "Spring" + i, "내용" + i, "link",
          source, baseTime.minusHours(i), i);
      UUID save = articleServiceImpl.save(articleBaseDto);
      System.out.println("save "+i+" = " + articleBaseDto);
    }

    // When: commentCount 기준 내림차순 페이징 (cursor = 30)
    ArticleCursorRequest reqComment = new ArticleCursorRequest();
    reqComment.setSortField(SortField.VIEW_COUNT);
    reqComment.setDirection(Direction.ASC);
    reqComment.setCursor("30");
    reqComment.setSize(10);


    List<ArticleBaseDto> resultComment = articleServiceImpl.findByCursorPaging(reqComment);
    System.out.println("resultComment = " + resultComment);

    // Then
    assertThat(resultComment).hasSize(10);
    assertThat(resultComment.get(0).getCommentCount()).isEqualTo(31);
    assertThat(resultComment.get(1).getCommentCount()).isEqualTo(32);
  }

  */

}