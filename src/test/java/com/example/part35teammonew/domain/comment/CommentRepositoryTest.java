package com.example.part35teammonew.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.comment.repository.CommentRepository;
import com.example.part35teammonew.domain.user.entity.User;
import com.example.part35teammonew.domain.user.repository.UserRepository;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 DB 사용
@TestPropertySource(locations = "classpath:application-dev.yml") // dev 설정 사용
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) //테스트 순서 지정하기 위해 사용
class CommentRepositoryTest {

  private static final Logger logger = LoggerFactory.getLogger(CommentRepositoryTest.class);

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private ArticleRepository articleRepository;

  @Autowired
  private UserRepository userRepository;

  private User testUser;
  private Article testArticle;
  private Comment testComment1;
  private Comment testComment2;
  private Comment testDeletedComment;

  /**
   * 리플렉션을 사용하여 protected 생성자에 접근하는 헬퍼 메서드
   */
  private <T> T createInstanceWithProtectedConstructor(Class<T> clazz) throws Exception {
    Constructor<T> constructor = clazz.getDeclaredConstructor();
    constructor.setAccessible(true);
    return constructor.newInstance();
  }

  @BeforeEach
  void setUp() {
    logger.info("==== 테스트 셋업 시작 ====");

    try {
      // 테스트 유저 생성
      testUser = User.create("test@example.com", "테스터", "password123");
      // createdAt과 updatedAt 필드 직접 설정
      java.lang.reflect.Field createdAtField = User.class.getDeclaredField("createdAt");
      createdAtField.setAccessible(true);
      createdAtField.set(testUser, LocalDateTime.now());

      java.lang.reflect.Field updatedAtField = User.class.getDeclaredField("updatedAt");
      updatedAtField.setAccessible(true);
      updatedAtField.set(testUser, LocalDateTime.now());

      userRepository.save(testUser); //테스트 유저 생성 완료

      // 리플렉션을 사용하여 Article 인스턴스 생성
      testArticle = createInstanceWithProtectedConstructor(Article.class);
      testArticle.setTitle("테스트 기사 제목");
      testArticle.setSummary("테스트 기사 요약");
      testArticle.setLink("https://example.com/test");
      testArticle.setSource("테스트 출처");
      testArticle.setDate(LocalDateTime.now());
      testArticle.setCreatedAt(LocalDateTime.now());
      articleRepository.save(testArticle); //"테스트 아티클 생성 완료

      // 테스트 댓글 생성
      testComment1 = Comment.create("테스트 댓글 1", testUser.getNickname(), testUser, testArticle);
      testComment2 = Comment.create("테스트 댓글 2", testUser.getNickname(), testUser, testArticle);
      testDeletedComment = Comment.create("삭제된 댓글", testUser.getNickname(), testUser, testArticle);
      testDeletedComment.delete(); // 삭제 상태로 변경

      // 댓글 저장
      commentRepository.save(testComment1);
      commentRepository.save(testComment2);
      commentRepository.save(testDeletedComment); //테스트 댓글 생성 완료
    } catch (Exception e) {
      throw new RuntimeException("테스트 셋업 실패", e);
    }

    logger.info("==== 테스트 셋업 완료 ====");
  }

  @Test
  @Order(1)
  @DisplayName("삭제되지 않은 댓글만 조회")
  void findByIsDeletedFalse() {
    logger.info("==== 삭제되지 않은 댓글 조회 테스트 시작 ====");

    // when
    List<Comment> nonDeletedComments = commentRepository.findByIsDeletedFalse();

    // then
    assertThat(nonDeletedComments).hasSize(2); // 삭제되지 않은 댓글 2개
    assertThat(nonDeletedComments).contains(testComment1, testComment2); //nonDeletedComments 리스트에  testComment1, testComment2 객체가 포함 확인
    assertThat(nonDeletedComments).doesNotContain(testDeletedComment); //nonDeletedComments 리스트에 testDeletedComment가 포함 되지 않았음 확인

    logger.info("==== 삭제되지 않은 댓글 조회 테스트 완료 ====");
    System.out.println("\n");

  }

  @Test
  @Order(2)
  @DisplayName("특정 기사의 삭제되지 않은 댓글 조회")
  void findByArticleIdAndIsDeletedFalse() {
    logger.info("==== 특정 기사의 삭제되지 않은 댓글 조회 테스트 시작 ====");

    // when
    List<Comment> articleComments = commentRepository.findByArticleIdAndIsDeletedFalse(testArticle.getId());

    // then
    assertThat(articleComments).hasSize(2); // 삭제되지 않은 댓글 2개
    assertThat(articleComments).contains(testComment1, testComment2); //리스트에  testComment1, testComment2 객체가 포함 확인
    assertThat(articleComments).doesNotContain(testDeletedComment); //리스트에 testDeletedComment가 포함 되지 않았음 확인

    logger.info("==== 특정 기사의 삭제되지 않은 댓글 조회 테스트 완료 ====");
    System.out.println("\n");

  }

  @Test
  @Order(3)
  @DisplayName("특정 유저의 삭제되지 않은 댓글 조회")
  void findByUserIdAndIsDeletedFalse() {
    logger.info("==== 특정 유저의 삭제되지 않은 댓글 조회 테스트 시작 ====");

    // when
    List<Comment> userComments = commentRepository.findByUserIdAndIsDeletedFalse(testUser.getId());

    // then
    assertThat(userComments).hasSize(2);
    assertThat(userComments).contains(testComment1, testComment2);
    assertThat(userComments).doesNotContain(testDeletedComment);

    logger.info("==== 특정 유저의 삭제되지 않은 댓글 조회 테스트 완료 ====");
    System.out.println("\n");

  }

  @Test
  @Order(4)
  @DisplayName("ID로 삭제되지 않은 댓글 조회")
  void findByIdAndIsDeletedFalse() {
    logger.info("==== ID로 삭제되지 않은 댓글 조회 테스트 시작 ====");

    // when
    Optional<Comment> foundComment = commentRepository.findByIdAndIsDeletedFalse(testComment1.getId());
    Optional<Comment> deletedComment = commentRepository.findByIdAndIsDeletedFalse(testDeletedComment.getId());

    // then
    assertThat(foundComment).isPresent();
    assertThat(foundComment.get()).isEqualTo(testComment1);
    assertThat(deletedComment).isEmpty(); // 삭제된 댓글은 조회되지 않아야 함

    logger.info("==== ID로 삭제되지 않은 댓글 조회 테스트 완료 ====");
    System.out.println("\n");

  }
}