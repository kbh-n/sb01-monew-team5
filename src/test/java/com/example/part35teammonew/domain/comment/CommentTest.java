package com.example.part35teammonew.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.user.entity.User;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) //테스트 순서 지정하기 위해 사용
class CommentTest {

  private static final Logger logger = LoggerFactory.getLogger(CommentTest.class);

  private Comment comment;
  private User user;
  private Article article;
  private String content;
  private String userNickname;

  @BeforeEach
  void setUp() throws Exception {
    logger.info("==== 테스트 셋업 시작 ====");

    // 실제 객체 생성
    user = User.create("test@example.com", "테스터", "password123");
    content = "테스트 댓글 내용입니다";
    userNickname = user.getNickname();

    // Article은 protected 생성자이므로 리플렉션으로 생성
    Constructor<Article> constructor = Article.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    article = constructor.newInstance();
    article.setTitle("제목");
    article.setSummary("요약");
    article.setLink("https://example.com");
    article.setSource("출처");
    article.setDate(LocalDateTime.now());
    article.setCreatedAt(LocalDateTime.now());

    comment = Comment.create(content, userNickname, user, article);

    logger.info("==== 테스트 셋업 완료 ====");
  }

  @Test
  @Order(1)
  @DisplayName("댓글 생성 시 초기값 설정 확인")
  void createComment() {
    logger.info("==== 댓글 생성 테스트 시작 ====");

    assertThat(comment.getContent()).isEqualTo(content); //content - 기대값:테스트 댓글 내용입니다 일치 여부 확인

    assertThat(comment.getUserNickname()).isEqualTo(userNickname); //userNickname - 기대값:테스터 일치 여부 확인

    assertThat(comment.getUser()).isEqualTo(user);//동일유저 확인

    assertThat(comment.getArticle()).isEqualTo(article);//동일기사 확인

    assertThat(comment.getLikeCount()).isEqualTo(0); //likeCount - 기대값: 0 일치 여부 확인

    assertThat(comment.isDeleted()).isFalse(); //isDeleted - 기대값: false 일치 여부 확인

    assertThat(comment.getCreatedAt()).isNotNull(); //createat 비어있는지 확인

    assertThat(comment.getUpdatedAt()).isNotNull();

    logger.info("==== 댓글 생성 테스트 완료 ====");
    System.out.println("\n");
  }

  @Test
  @Order(2)
  @DisplayName("댓글 내용 수정 확인")
  void updateContent() {
    logger.info("==== 댓글 내용 수정 테스트 시작 ====");

    String newContent = "수정된 댓글 내용"; //수정 전: content=테스트 댓글 내용입니다

    comment.updateContent(newContent); //수정 후: content=수정된 댓글 내용

    assertThat(comment.getContent()).isEqualTo(newContent); //content - 기대값:수정된 댓글 내용

    logger.info("==== 댓글 내용 수정 테스트 완료 ====");
    System.out.println("\n");
  }

  @Test
  @Order(3)
  @DisplayName("댓글 삭제 상태 변경 확인")
  void delete() {
    logger.info("==== 댓글 삭제 테스트 시작 ====");
    //삭제 전: isDeleted=false

    comment.delete(); //삭제 후: isDeleted=true

    assertThat(comment.isDeleted()).isTrue(); //isDeleted - 기대값: true

    logger.info("==== 댓글 삭제 테스트 완료 ====");
    System.out.println("\n");
  }

}