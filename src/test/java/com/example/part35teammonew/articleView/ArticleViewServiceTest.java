package com.example.part35teammonew.articleView;


import static org.assertj.core.api.Assertions.assertThat;

import com.example.part35teammonew.domain.articleView.entity.ArticleView;
import com.example.part35teammonew.domain.articleView.mapper.ArticleViewMapper;
import com.example.part35teammonew.domain.articleView.repository.ArticleViewRepository;
import com.example.part35teammonew.domain.articleView.service.ArticleViewServiceImp;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

@DataMongoTest
@Import({ArticleViewServiceImp.class, ArticleViewServiceTest.MapperMockConfig.class})
class ArticleViewServiceTest {

  @Autowired
  private ArticleViewServiceImp articleViewService;

  @Autowired
  private ArticleViewRepository articleViewRepository;

  private UUID articleId;
  private UUID userId;
  private UUID userId1;
  private UUID userId3;

  @BeforeEach
  void setUp() {
    articleViewRepository.deleteAll();
    articleId = UUID.randomUUID();
    userId = UUID.randomUUID();
    userId1 = UUID.randomUUID();
    userId3 = UUID.randomUUID();

    ArticleView articleView = ArticleView.setUpNewArticleView(articleId);
    articleViewRepository.save(articleView);
  }

  @TestConfiguration
  static class MapperMockConfig {

    @Bean
    public ArticleViewMapper articleViewMapper() {
      return Mockito.mock(ArticleViewMapper.class);
    }
  }

  @Test
  @DisplayName("addReadUser 호출")
  void addReadUser_success() {
    boolean result = articleViewService.addReadUser(articleId, userId);

    ArticleView updated = articleViewRepository.findByArticleId(articleId).get();

    assertThat(result).isTrue();
    assertThat(updated.getReadUserIds()).contains(userId);
  }

  @Test
  @DisplayName("읽은 유저수 반환")
  void countReadUser_success() {
    articleViewService.addReadUser(articleId, userId);
    articleViewService.addReadUser(articleId, userId1);
    long count = articleViewService.countReadUser(articleId);

    assertThat(count).isEqualTo(2);
  }

  @Test
  @DisplayName("읽은 유저수 반환")
  void duplicateReadUser_success() {
    articleViewService.addReadUser(articleId, userId);
    articleViewService.addReadUser(articleId, userId1);
    articleViewService.addReadUser(articleId, userId1);
    articleViewService.addReadUser(articleId, userId3);
    long count = articleViewService.countReadUser(articleId);

    assertThat(count).isEqualTo(3);
  }

  @Test
  @DisplayName("조회수 같음, 내림차순")
  void getArticles_sameCount_sortByObjectIdDescending() {
    UUID articleId1 = UUID.randomUUID();
    UUID articleId2 = UUID.randomUUID();

    ArticleView v1 = ArticleView.setUpNewArticleView(articleId1);
    v1.addNewReader(UUID.randomUUID());

    try {
      Thread.sleep(10);
    } catch (InterruptedException ignored) {
    }

    ArticleView v2 = ArticleView.setUpNewArticleView(articleId2);
    v2.addNewReader(UUID.randomUUID());

    articleViewRepository.saveAll(List.of(v1, v2));

    List<UUID> actual = articleViewService.getSortByVewCountPageNation(null, Pageable.ofSize(2),
        "desc");

    List<ArticleView> sorted = articleViewRepository.findAll().stream()
        .filter(v -> v.getCount().equals(1L))
        .sorted(
            (a, b) -> b.getId().toHexString().compareTo(a.getId().toHexString()))
        .toList();

    List<UUID> expected = sorted.stream()
        .map(ArticleView::getArticleId)
        .toList();
    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test
  @DisplayName("내림차순 + limit")
  void getArticles_sortedByCount_limitApplied() {
    articleViewRepository.deleteAll();

    UUID a1 = UUID.randomUUID();
    UUID a2 = UUID.randomUUID();
    UUID a3 = UUID.randomUUID();

    ArticleView v1 = ArticleView.setUpNewArticleView(a1);
    v1.addNewReader(UUID.randomUUID()); // 1

    ArticleView v2 = ArticleView.setUpNewArticleView(a2);
    v2.addNewReader(UUID.randomUUID());
    v2.addNewReader(UUID.randomUUID()); // 2

    ArticleView v3 = ArticleView.setUpNewArticleView(a3);
    // 0

    articleViewRepository.saveAll(List.of(v1, v2, v3));

    List<UUID> result = articleViewService.getSortByVewCountPageNation(null, Pageable.ofSize(2),
        "desc");

    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(v2.getArticleId(), v1.getArticleId());
  }

  @Test
  @DisplayName("커서보다 작은거 반환")
  void getArticles_filteredByCursorCount() {
    articleViewRepository.deleteAll();

    UUID a1 = UUID.randomUUID();
    UUID a2 = UUID.randomUUID();
    UUID a3 = UUID.randomUUID();

    ArticleView v1 = ArticleView.setUpNewArticleView(a1);
    v1.addNewReader(UUID.randomUUID());

    ArticleView v2 = ArticleView.setUpNewArticleView(a2);
    v2.addNewReader(UUID.randomUUID());
    v2.addNewReader(UUID.randomUUID());

    ArticleView v3 = ArticleView.setUpNewArticleView(a3);
    articleViewRepository.saveAll(List.of(v1, v2, v3));

    List<UUID> result = articleViewService.getSortByVewCountPageNation(1L, Pageable.ofSize(5),
        "desc");

    assertThat(result).containsExactly(v3.getArticleId());
  }

  @Test
  @DisplayName("오름차순 +  limit")
  void getArticles_sortedByCountAscending() {
    articleViewRepository.deleteAll();

    UUID a1 = UUID.randomUUID();
    UUID a2 = UUID.randomUUID();
    UUID a3 = UUID.randomUUID();

    ArticleView v1 = ArticleView.setUpNewArticleView(a1);
    v1.addNewReader(UUID.randomUUID()); // 1

    ArticleView v2 = ArticleView.setUpNewArticleView(a2);
    v2.addNewReader(UUID.randomUUID());
    v2.addNewReader(UUID.randomUUID()); // 2

    ArticleView v3 = ArticleView.setUpNewArticleView(a3);//0

    articleViewRepository.saveAll(List.of(v1, v2, v3));

    List<UUID> result = articleViewService.getSortByVewCountPageNation(null, Pageable.ofSize(3),
        "asc");

    List<UUID> expected = articleViewRepository.findAll().stream()
        .sorted(Comparator.comparingLong(ArticleView::getCount)
            .thenComparing(x -> x.getId().toHexString()))
        .map(ArticleView::getArticleId)
        .limit(3)
        .toList();

    assertThat(result).containsExactlyElementsOf(expected);
  }
}
