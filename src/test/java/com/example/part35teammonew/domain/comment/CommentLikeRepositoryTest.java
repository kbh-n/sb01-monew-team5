package com.example.part35teammonew.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.comment.entity.CommentLike;
import com.example.part35teammonew.domain.comment.repository.CommentLikeRepository;
import com.example.part35teammonew.domain.comment.repository.CommentRepository;
import com.example.part35teammonew.domain.user.entity.User;
import com.example.part35teammonew.domain.user.repository.UserRepository;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
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
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 DB 사용
@TestPropertySource(locations = "classpath:application-dev.yml") // dev 설정 사용
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) //테스트 순서 지정하기 위해 사용
@Transactional
class CommentLikeRepositoryTest {

  private static final Logger logger = LoggerFactory.getLogger(CommentLikeRepositoryTest.class);

  @Autowired
  private CommentLikeRepository commentLikeRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private ArticleRepository articleRepository;

  @Autowired
  private UserRepository userRepository;

  private User testUser1;
  private User testUser2;
  private Article testArticle;
  private Comment testComment;

  /**
   * 리플렉션을 사용하여 Article protected 생성자에 접근하는 헬퍼 메서드
   */
  private <T> T createInstanceWithProtectedConstructor(Class<T> clazz) throws Exception {
    Constructor<T> constructor = clazz.getDeclaredConstructor();
    constructor.setAccessible(true);
    return constructor.newInstance();
  }

  @BeforeEach
  void setUp() {
    logger.info("==== 좋아요 리포지토리 테스트 셋업 시작 ====");

    try {
      // 테스트용 User 생성 및 저장
      testUser1 = User.create("user1@example.com", "테스터1", "password123");
      testUser2 = User.create("user2@example.com", "테스터2", "password456");

      // createdAt과 updatedAt 필드 직접 설정 (User1)
      java.lang.reflect.Field createdAtField1 = User.class.getDeclaredField("createdAt");
      createdAtField1.setAccessible(true);
      createdAtField1.set(testUser1, LocalDateTime.now());

      java.lang.reflect.Field updatedAtField1 = User.class.getDeclaredField("updatedAt");
      updatedAtField1.setAccessible(true);
      updatedAtField1.set(testUser1, LocalDateTime.now());

      // createdAt과 updatedAt 필드 직접 설정 (User2)
      java.lang.reflect.Field createdAtField2 = User.class.getDeclaredField("createdAt");
      createdAtField2.setAccessible(true);
      createdAtField2.set(testUser2, LocalDateTime.now());

      java.lang.reflect.Field updatedAtField2 = User.class.getDeclaredField("updatedAt");
      updatedAtField2.setAccessible(true);
      updatedAtField2.set(testUser2, LocalDateTime.now());

      userRepository.save(testUser1);
      userRepository.save(testUser2);

      // 리플렉션을 사용하여 Article 인스턴스 생성
      testArticle = createInstanceWithProtectedConstructor(Article.class);
      testArticle.setTitle("테스트 게시글");
      testArticle.setSummary("테스트 요약");
      testArticle.setLink("https://test-link.com");
      testArticle.setSource("테스트 소스");
      testArticle.setDate(LocalDateTime.now());
      testArticle.setCreatedAt(LocalDateTime.now());
      articleRepository.save(testArticle);
      logger.info("테스트 게시글 생성: id={}", testArticle.getId());

      // 테스트용 Comment 생성 및 저장
      testComment = Comment.create("테스트 댓글", testUser1.getNickname(), testUser1, testArticle);
      commentRepository.save(testComment);

    } catch (Exception e) {
      throw new RuntimeException("테스트 셋업 실패", e);
    }

    logger.info("==== 좋아요 리포지토리 테스트 셋업 완료 ====");
  }

  @Test
  @Order(1)
  @DisplayName("유저ID와 댓글ID로 좋아요 찾기")
  void findByUserIdAndCommentId() {
    logger.info("==== 유저ID와 댓글ID로 좋아요 찾기 테스트 시작 ====");

    // given
    CommentLike commentLike = CommentLike.create(testComment, testUser1);
    commentLike.setArticle(testArticle);
    commentLikeRepository.save(commentLike);

    // when
    Optional<CommentLike> foundLike = commentLikeRepository.findByUserIdAndCommentId( //testcomment에 testUser1는 좋아요를 누름
        testUser1.getId(), testComment.getId());
    Optional<CommentLike> notFoundLike = commentLikeRepository.findByUserIdAndCommentId( //testcomment에 testUser2는 좋아요를 누르지 않음
        testUser2.getId(), testComment.getId());

    // then
    assertThat(foundLike).isPresent(); //user1 좋아요 존재 여부 - 기대값: true
    assertThat(foundLike.get().getUser().getId()).isEqualTo(testUser1.getId());  //user1 좋아요 사용자 ID 일치여부
    assertThat(foundLike.get().getComment().getId()).isEqualTo(testComment.getId()); //user1 좋아요 댓글 ID 일치여부
    assertThat(notFoundLike).isEmpty(); //user2 좋아요 존재 여부 - 기대값: false

    logger.info("==== 유저ID와 댓글ID로 좋아요 찾기 테스트 완료 ====");
    System.out.println("\n");
  }

  @Test
  @Order(2)
  @DisplayName("댓글별 좋아요 개수 카운트")
  void countByCommentId() {
    logger.info("==== 댓글별 좋아요 개수 카운트 테스트 시작 ====");

    // given
    CommentLike commentLike1 = CommentLike.create(testComment, testUser1); //testComment에 testUser1는 좋아요를 누름
    commentLike1.setArticle(testArticle);
    commentLikeRepository.save(commentLike1);

    CommentLike commentLike2 = CommentLike.create(testComment, testUser2); //testComment에 testUser2도 좋아요를 누름
    commentLike2.setArticle(testArticle);
    commentLikeRepository.save(commentLike2);

    // when
    long likeCount = commentLikeRepository.countByCommentId(testComment.getId()); //testComment에 달린 좋아요 수 카운트

    // then
    assertThat(likeCount).isEqualTo(2); //댓글 좋아요 수 - 기대값: 2 일치 여부

    logger.info("==== 댓글별 좋아요 개수 카운트 테스트 완료 ====");
    System.out.println("\n");
  }

  @Test
  @Order(3)
  @DisplayName("댓글별 좋아요 전체 삭제")
  void deleteAllByCommentId() { //댓글 삭제 시 연관된 모든 좋아요 삭제
    logger.info("==== 댓글별 좋아요 전체 삭제 테스트 시작 ====");

    // given
    CommentLike commentLike1 = CommentLike.create(testComment, testUser1);
    commentLike1.setArticle(testArticle);
    commentLikeRepository.save(commentLike1);

    CommentLike commentLike2 = CommentLike.create(testComment, testUser2);
    commentLike2.setArticle(testArticle);
    commentLikeRepository.save(commentLike2);

    long beforeDeleteCount = commentLikeRepository.countByCommentId(testComment.getId());
    logger.info("검증: 삭제 전 좋아요 수 - 기대값: 2, 실제값: {}", beforeDeleteCount);
    assertThat(beforeDeleteCount).isEqualTo(2); //삭제 전 좋아요 수 - 기대값: 2 일치여부

    // when
    commentLikeRepository.deleteAllByCommentId(testComment.getId());

    // then
    long afterDeleteCount = commentLikeRepository.countByCommentId(testComment.getId());
    assertThat(afterDeleteCount).isEqualTo(0); //삭제 후 좋아요 수 - 기대값: 0 일치여부

    logger.info("==== 댓글별 좋아요 전체 삭제 테스트 완료 ====");
    System.out.println("\n");
  }

  @Test
  @Order(4)
  @DisplayName("좋아요 삭제 후 count 감소 확인")
  void deleteCommentLikeAndDecrementCount() { //특정 유저가 단 좋아요 1개 삭제
    logger.info("==== 좋아요 삭제 후 count 감소 테스트 시작 ====");

    // given
    CommentLike commentLike = CommentLike.create(testComment, testUser1);
    commentLike.setArticle(testArticle);
    commentLikeRepository.save(commentLike);

    testComment.incrementLikeCount(); // 좋아요 수 증가 (엔티티 상태 반영)
    testComment.incrementLikeCount(); // 좋아요 수 증가 (엔티티 상태 반영)
    assertThat(testComment.getLikeCount()).isEqualTo(2); //좋아요2

    // when - 좋아요 삭제
    commentLikeRepository.deleteById(commentLike.getId());
    testComment.decrementLikeCount(); // 직접 감소 (엔티티 상태 반영)

    // then
    assertThat(testComment.getLikeCount()).isEqualTo(1); // 엔터티 상태 감소 확인

    logger.info("==== 좋아요 삭제 후 count 감소 테스트 완료 ====");
    System.out.println("\n");
  }

}