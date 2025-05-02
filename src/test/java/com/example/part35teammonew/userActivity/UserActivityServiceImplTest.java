package com.example.part35teammonew.userActivity;

import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.part35teammonew.domain.userActivity.Dto.ArticleInfoView;
import com.example.part35teammonew.domain.userActivity.Dto.InterestView;
import com.example.part35teammonew.domain.userActivity.Dto.LikeCommentView;
import com.example.part35teammonew.domain.userActivity.Dto.RecentCommentView;
import com.example.part35teammonew.domain.userActivity.Dto.UserActivityDto;
import com.example.part35teammonew.domain.userActivity.Dto.UserInfoDto;
import com.example.part35teammonew.domain.userActivity.entity.UserActivity;
import com.example.part35teammonew.domain.userActivity.repository.UserActivityRepository;
import com.example.part35teammonew.domain.userActivity.service.UserActivityServiceImpl;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserActivityServiceImplTest {

  @Autowired
  private UserActivityServiceImpl userActivityService;

  @Autowired
  private UserActivityRepository userActivityRepository;

  private UUID userId;
  private UserActivity userActivity;

  /*@BeforeEach
  void setUp() {
    userActivityRepository.deleteAll();
    userId = UUID.randomUUID();
    userActivity = UserActivity.setUpNewUserActivity(Instant.now(), userId, "닉닉네네임임",
        "test@test.com");
    userActivityRepository.save(userActivity);
  }*/

  @Test
  @DisplayName("생성 확인")
  void testAddInterestViewSuccess() {
    InterestView interest = InterestView.builder()
        .interestId(UUID.randomUUID())
        .interestName("테스트 관심사")
        .interestKeywords(List.of("java", "spring"))
        .interestSubscriberCount(123L)
        .createdAt(Instant.now())
        .build();

    userActivityService.addInterestView(userId, interest);
    UserActivity updated = userActivityRepository.findByUserId(userId).orElseThrow();
    assertTrue(updated.getSubscriptions().contains(interest));
  }

  @Test
  @DisplayName("없는거 찾기")
  void testAddInterestViewNotFound() {
    InterestView interest = InterestView.builder().interestId(UUID.randomUUID()).build();
    assertThrows(RuntimeException.class,
        () -> userActivityService.addInterestView(UUID.randomUUID(), interest));
  }

  @Test
  @DisplayName("구독 해제")
  void testSubtractInterestViewSuccess() {
    InterestView interest = InterestView.builder().interestId(UUID.randomUUID()).build();
    userActivityService.addInterestView(userId, interest);
    userActivityService.subtractInterestView(userId, interest);
    UserActivity updated = userActivityRepository.findByUserId(userId).orElseThrow();
    assertTrue(!updated.getSubscriptions().contains(interest));
  }

  @Test
  @DisplayName("ㅇ어븐거 구독 해제")
  void testSubtractInterestViewNotFound() {
    InterestView interest = InterestView.builder().interestId(UUID.randomUUID()).build();
    assertThrows(RuntimeException.class,
        () -> userActivityService.subtractInterestView(UUID.randomUUID(), interest));
  }

  @Test
  @DisplayName("최근 댓들 추가")
  void testAddRecentCommentViewSuccess() {
    RecentCommentView comment = RecentCommentView.builder()
        .id(UUID.randomUUID())
        .articleId(UUID.randomUUID())
        .articleTitle("제목")
        .userId(userId)
        .userNickname("닉네임")
        .content("댓글 내용")
        .likeCount(10)
        .createdAt(LocalDateTime.now())
        .build();

    userActivityService.addRecentCommentView(userId, comment);
    UserActivity updated = userActivityRepository.findByUserId(userId).orElseThrow();
    assertTrue(
        updated.getRecentcomments().stream().anyMatch(c -> c.getId().equals(comment.getId()))
    );
  }

  @Test
  @DisplayName("죄근댓글 없는데 추가")
  void testAddRecentCommentViewNotFound() {
    RecentCommentView comment = RecentCommentView.builder().id(UUID.randomUUID()).build();
    assertThrows(RuntimeException.class,
        () -> userActivityService.addRecentCommentView(UUID.randomUUID(), comment));
  }

  @Test
  @DisplayName("대들에 좋아요")
  void testAddLikeCommentViewSuccess() {
    LikeCommentView like = LikeCommentView.builder()
        .id(UUID.randomUUID())
        .commentId(UUID.randomUUID())
        .articleId(UUID.randomUUID())
        .articleTitle("제목")
        .commentUserId(UUID.randomUUID())
        .commentUserNickname("댓글작성자")
        .commentContent("내용")
        .commentLikeCount(5)
        .commentCreatedAt(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .build();

    userActivityService.addLikeCommentView(userId, like);
    UserActivity updated = userActivityRepository.findByUserId(userId).orElseThrow();
    assertTrue(updated.getLikeComment().contains(like));
  }

  @Test
  @DisplayName("없는 댓글 좋아요")
  void testAddLikeCommentViewNotFound() {
    LikeCommentView like = LikeCommentView.builder().id(UUID.randomUUID()).build();
    assertThrows(RuntimeException.class,
        () -> userActivityService.addLikeCommentView(UUID.randomUUID(), like));
  }

  @Test
  @DisplayName("기사 봄")
  void testAddArticleInfoViewSuccess() {
    ArticleInfoView article = ArticleInfoView.builder()
        .id(UUID.randomUUID())
        .viewedBy(userId)
        .createdAt(Instant.now())
        .articleId(UUID.randomUUID())
        .source("뉴스")
        .sourceUrl("주소")
        .articleTitle("기사")
        .articlePublishedDate(LocalDateTime.now())
        .articleSummary("요약")
        .articleCommentCount(2)
        .articleViewCount(100L)
        .build();

    userActivityService.addArticleInfoView(userId, article);
    UserActivity updated = userActivityRepository.findByUserId(userId).orElseThrow();
    assertTrue(updated.getArticleViews().contains(article));
  }

  @Test
  @DisplayName("없는 기사 봄")
  void testAddArticleInfoViewNotFound() {
    ArticleInfoView article = ArticleInfoView.builder().id(UUID.randomUUID()).build();
    assertThrows(RuntimeException.class,
        () -> userActivityService.addArticleInfoView(UUID.randomUUID(), article));
  }

  /*@Test
  @DisplayName("닉네임 업데이트")
  void testUpdateUserInformationSuccess() {
    UserInfoDto dto = new UserInfoDto();
    dto.setNickName("닉네임");

    UserActivityDto result = userActivityService.updateUserInformation(userId, dto);

    assertNotNull(result);
    assertEquals("닉네임", result.getNickName());

    UserActivity updated = userActivityRepository.findByUserId(userId).orElseThrow();
    assertEquals("닉네임", updated.getNickName());
  }*/

  /*@Test
  @DisplayName("없는 유저 ㅓ데이트")
  void testUpdateUserInformationNotFound() {
    UUID fakeId = UUID.randomUUID();
    UserInfoDto dto = new UserInfoDto();
    dto.setNickName("닉네임");

    assertThrows(
        NoSuchElementException.class, () -> userActivityService.updateUserInformation(fakeId, dto));
  }*/

  @Test
  @DisplayName("물리적 삭제")
  void testDeleteUserActivitySuccess() {
    userActivityService.deleteUserActivity(userId);
    Optional<UserActivity> result = userActivityRepository.findByUserId(userId);
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("없는거 물리적 삭제")
  void testDeleteUserActivityWhenUserDoesNotExist() {
    UUID nonExistentId = UUID.randomUUID();
    userActivityService.deleteUserActivity(nonExistentId);
    assertTrue(true);
  }
}
