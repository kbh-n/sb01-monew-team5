package com.example.part35teammonew.articleView;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.part35teammonew.domain.articleView.entity.ArticleView;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArticleViewTest {

  private UUID articleId;
  private UUID reader1;
  private UUID reader2;
  private ArticleView articleView;

  @BeforeEach
  void setUp() {
    articleId = UUID.randomUUID();
    reader1 = UUID.randomUUID();
    reader2 = UUID.randomUUID();
    articleView = ArticleView.setUpNewArticleView(articleId);
  }

  @Test
  @DisplayName("생성시 비어있어야함")
  void createNewArticleView() {
    assertThat(articleView.getArticleId()).isEqualTo(articleId);
    assertThat(articleView.getReadUserIds()).isEmpty();
  }

  @Test
  @DisplayName("저장된 아이디 확인")
  void addNewReader_success() {
    articleView.addNewReader(reader1);

    assertThat(articleView.getReadUserIds()).contains(reader1);
  }

  @Test
  @DisplayName("중복 유저 추가 확인")
  void addDuplicateReader() {
    articleView.addNewReader(reader1);
    articleView.addNewReader(reader1);

    Set<UUID> readers = articleView.getReadUserIds();
    assertThat(readers).hasSize(1);
    assertThat(readers).contains(reader1);
  }

  @Test
  @DisplayName("여러 유저를 추가확인")
  void addMultipleReaders() {
    articleView.addNewReader(reader1);
    articleView.addNewReader(reader2);

    Set<UUID> readers = articleView.getReadUserIds();
    assertThat(readers).hasSize(2);
    assertThat(readers).contains(reader1, reader2);
  }
}
