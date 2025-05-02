package com.example.part35teammonew.articleView;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.part35teammonew.domain.articleView.entity.ArticleView;
import com.example.part35teammonew.domain.articleView.repository.ArticleViewRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

@DataMongoTest
class ArticleViewRepositoryTest {

  @Autowired
  private ArticleViewRepository repository;

  @Test
  @DisplayName("ID로 ArticleView 조회")
  void findByArticleId_success() {
    UUID articleId = UUID.randomUUID();
    ArticleView articleView = ArticleView.setUpNewArticleView(articleId);
    repository.save(articleView);

    Optional<ArticleView> result = repository.findByArticleId(articleId);

    assertThat(result).isPresent();
    assertThat(result.get().getArticleId()).isEqualTo(articleId);
    assertThat(result.get().getReadUserIds()).isEmpty();
  }

  @Test
  @DisplayName("없는 기사 조회 시 빈거 반환")
  void findByArticleId_fail() {
    Optional<ArticleView> result = repository.findByArticleId(UUID.randomUUID());

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("저장 후 조회")
  void addNewReaderAndUpdate() {
    UUID articleId = UUID.randomUUID();
    UUID readerId = UUID.randomUUID();
    ArticleView articleView = ArticleView.setUpNewArticleView(articleId);
    articleView.addNewReader(readerId);
    repository.save(articleView);

    Optional<ArticleView> result = repository.findByArticleId(articleId);

    assertThat(result).isPresent();
    assertThat(result.get().getReadUserIds()).containsExactly(readerId);
  }

  @Test
  @DisplayName("삭제 확인")
  void deleteArticleView() {
    UUID articleId = UUID.randomUUID();
    ArticleView articleView = ArticleView.setUpNewArticleView(articleId);
    repository.save(articleView);

    repository.delete(articleView);

    Optional<ArticleView> result = repository.findByArticleId(articleId);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("한 ArticleView에 여러유저 아이디 추가후 확인")
  void addDuplicateReaderOnlyOnce() {
    UUID articleId = UUID.randomUUID();
    UUID readerId = UUID.randomUUID();
    ArticleView articleView = ArticleView.setUpNewArticleView(articleId);
    articleView.addNewReader(readerId);
    articleView.addNewReader(readerId);
    repository.save(articleView);

    Optional<ArticleView> result = repository.findByArticleId(articleId);

    assertThat(result).isPresent();
    assertThat(result.get().getReadUserIds()).hasSize(1);
    assertThat(result.get().getReadUserIds()).contains(readerId);
  }
}
