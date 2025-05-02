package com.example.part35teammonew.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.comment.entity.CommentLike;
import com.example.part35teammonew.domain.user.entity.User;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) //테스트 순서 지정하기 위해 사용
class CommentLikeTest {

  private static final Logger logger = LoggerFactory.getLogger(CommentLikeTest.class);
  private UUID userId;
  private Comment comment;
  private User user;
  private Article article;
  private String content;
  private String userNickname;

  @BeforeEach
  void setUp() throws Exception {
    logger.info("==== 테스트 셋업 시작 ====");

    user = User.create("tester@example.com", "테스터", "password");
    userId = user.getId();
    userNickname = user.getNickname();

    Constructor<Article> constructor = Article.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    article = constructor.newInstance();
    article.setTitle("제목");
    article.setSummary("요약");
    article.setLink("https://example.com");
    article.setSource("출처");
    article.setDate(LocalDateTime.now());
    article.setCreatedAt(LocalDateTime.now());

    content = "테스트 댓글 내용입니다";
    comment = Comment.create(content, userNickname, user, article);

    logger.info("==== 테스트 셋업 완료 ====");
  }


  @Test
  @Order(1)
  @DisplayName("댓글 좋아요 생성 및 좋아요 수 증가 확인")
  void createAndIncrementLikeCount() {
    logger.info("==== 댓글 좋아요 생성 및 좋아요 수 증가 테스트 시작 ====");

    // given
    int initialLikeCount = comment.getLikeCount(); //좋아요 생성 전 기대값: 0

    //좋아요 생성 및 카운트 증가
    CommentLike commentLike = CommentLike.create(comment, user);
    commentLike.setArticle(article);
    comment.incrementLikeCount(); //좋아요 생성 및 카운트 증가 후 기대값: 1

    // 좋아요 정보 검증
    assertThat(commentLike.getComment()).isEqualTo(comment); //comment 정보 일치 확인
    assertThat(commentLike.getUser()).isEqualTo(user); //user 정보 일치 확인
    assertThat(commentLike.getUser().getNickname()).isEqualTo(userNickname); //user-nickname 정보 일치 확인
    assertThat(commentLike.getArticle()).isEqualTo(article); //article 정보 일치 확인
    assertThat(commentLike.getCreatedAt()).isNotNull(); //createdAt 비었는지 확인

    // 좋아요 카운트 증가 검증
    assertThat(comment.getLikeCount()).isEqualTo(initialLikeCount + 1); // likeCount 기대값: 1

    logger.info("==== 댓글 좋아요 생성 및 좋아요 수 증가 테스트 완료 ====");
    System.out.println("\n");
  }

  @Test
  @Order(2)
  @DisplayName("좋아요 감소 확인")
  void decrementLikeCount() {
    logger.info("==== 좋아요 감소 테스트 시작 ====");

    // 좋아요 먼저 증가시키고
    comment.incrementLikeCount();
    comment.incrementLikeCount();
    int likeCountAfterIncrement = comment.getLikeCount();
    //증가 후/감소 전 기대값: 2


    // 좋아요 감소
    comment.decrementLikeCount(); //감소 후: likeCount=1
    assertThat(comment.getLikeCount()).isEqualTo(likeCountAfterIncrement - 1); //likeCount - 기대값:1 검증

    logger.info("==== 좋아요 감소 테스트 완료 ====");
    System.out.println("\n");
  }

  @Test
  @Order(3)
  @DisplayName("좋아요 감소 시 음수가 되지 않음")
  void decrementLikeCount_notBelowZero() {
    logger.info("==== 좋아요 감소 시 음수가 되지 않음 테스트 시작 ====");

    //초기 likeCount = 0
    // 좋아요 개수가 0일 때
    comment.decrementLikeCount(); //감소 시도 후 likeCount:0

    // 여전히 0이어야 함
    assertThat(comment.getLikeCount()).isEqualTo(0); //likeCount - 기대값: 0

    logger.info("==== 좋아요 감소 시 음수가 되지 않음 테스트 완료 ====");
    System.out.println("\n");
  }
}