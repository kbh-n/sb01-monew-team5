package com.example.part35teammonew.domain.article.repository;

import com.example.part35teammonew.domain.article.dto.ArticleBaseDto;
import com.example.part35teammonew.domain.article.entity.Article;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import jdk.jfr.Name;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ArticleRepositoryTest {

  @Autowired
  ArticleRepository articleRepository;

  private Article createSampleArticle(String title) {
    ArticleBaseDto dto = new ArticleBaseDto(
        UUID.randomUUID(),
        title,
        "summary",
        "link",
        "source",
        LocalDateTime.now(),null, 0
    );
    return new Article(dto);
  }

  @Test
  @Name("저장 로직 테스트")
  public void save() {
    Article article = createSampleArticle("title");
    articleRepository.save(article);
    Assertions.assertThat(article.getId()).isNotNull();
    Assertions.assertThat(article.getTitle()).isEqualTo("title");
  }

  @Test
  @Name("단 건 조회")
  public void findOne() {
    Article article = createSampleArticle("title");
    articleRepository.save(article);
    Optional<Article> result = articleRepository.findById(article.getId());
    Assertions.assertThat(result).isPresent();
    Assertions.assertThat(result.get().getTitle()).isEqualTo("title");
  }

  @Test
  @Name("전체 조회")
  public void findAll() {
    articleRepository.save(createSampleArticle("title1"));
    articleRepository.save(createSampleArticle("title2"));

    var result = articleRepository.findAll();
    Assertions.assertThat(result).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  @Name("수정 테스트")
  public void update() {
    Article article = createSampleArticle("original");
    articleRepository.save(article);

    // 수정
    article.update(new ArticleBaseDto(UUID.randomUUID(),"updated title", "updated summary", "updated link", "updated source", LocalDateTime.now(),null, 0));
    articleRepository.save(article);

    Optional<Article> updated = articleRepository.findById(article.getId());
    Assertions.assertThat(updated).isPresent();
    Assertions.assertThat(updated.get().getTitle()).isEqualTo("updated title");
  }

  @Test
  @Name("삭제 테스트")
  public void delete() {
    Article article = createSampleArticle("to delete");
    articleRepository.save(article);
    UUID id = article.getId();

    articleRepository.deleteById(id);

    Optional<Article> result = articleRepository.findById(id);
    Assertions.assertThat(result).isEmpty();
  }
}
