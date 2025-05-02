package com.example.part35teammonew.domain.interest.repository;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import com.example.part35teammonew.domain.interest.entity.Interest;
import com.example.part35teammonew.domain.interest.repository.InterestRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
public class InterestRepositoryTest {

  @Autowired
  private InterestRepository repo;

  @BeforeEach
  void setUp() {
    repo.deleteAll();

    Interest i1 = new Interest();
    i1.setName("Java Spring");
    i1.setKeywords("java,spring");
    repo.save(i1);

    Interest i2 = new Interest();
    i2.setName("Python Basics");
    i2.setKeywords("python,AI");
    repo.save(i2);

    Interest i3 = new Interest();
    i3.setName("Spring Boot");
    i3.setKeywords("boot,spring");
    repo.save(i3);
  }

  @Test
  @DisplayName("findAllNames() : 저장된 모든 관심사(interest) 이름만 조회")
  void testFindAllNames() {
    List<String> names = repo.findAllNames();

    Assertions.assertThat(names)
        .hasSize(3)
        .contains("Java Spring", "Python Basics", "Spring Boot");
  }

  @Test
  @DisplayName("searchByNameOrKeyword(): 관심사 이름 기준 부분 일치 & 페이징")
  void testSearchByNameOrKeyword_nameMatch() {
    Pageable page = PageRequest.of(0, 10, Sort.by("name").ascending());
    Page<Interest> result = repo.searchByNameOrKeyword("spring", page);

    Assertions.assertThat(result.getTotalElements()).isEqualTo(2);

    Assertions.assertThat(result.getContent())
        .extracting(Interest::getName)
        .containsExactly("Java Spring", "Spring Boot");
  }

  @Test
  @DisplayName("searchByNameOrKeyword(): 관심사 키워드 기준 부분 일치 & 페이징")
  void testSearchByNameOrKeyword_keywordMatch() {
    Pageable page = PageRequest.of(0, 10, Sort.by("keywords").ascending());

    Page<Interest> result = repo.searchByNameOrKeyword("ai", page);

    Assertions.assertThat(result.getTotalElements()).isEqualTo(1);

    Assertions.assertThat(result.getContent().get(0).getName()).isEqualTo("Python Basics");

  }

}
