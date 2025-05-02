package com.example.part35teammonew.domain.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import com.example.part35teammonew.domain.comment.dto.CommentCreateRequest;
import com.example.part35teammonew.domain.comment.dto.CommentDto;
import com.example.part35teammonew.domain.comment.dto.CommentLikeResponse;
import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.comment.entity.CommentLike;
import com.example.part35teammonew.domain.comment.mapper.CommentMapper;
import com.example.part35teammonew.domain.comment.repository.CommentLikeRepository;
import com.example.part35teammonew.domain.comment.repository.CommentRepository;
import com.example.part35teammonew.domain.user.entity.User;
import com.example.part35teammonew.domain.user.repository.UserRepository;

import com.example.part35teammonew.exeception.comment.CommentLikeConflict;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentLikeServiceTest {

  private static final Logger logger = LoggerFactory.getLogger(CommentLikeServiceTest.class);

  @InjectMocks
  private CommentLikeServiceImpl commentLikeService;

  @Mock
  private CommentLikeRepository commentLikeRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ArticleRepository articleRepository;

  @Mock
  private CommentMapper commentMapper;

  private User testUser;
  private Article testArticle;
  private Comment testComment;
  private CommentLike testCommentLike;
  private UUID testUserId;
  private UUID testArticleId;
  private UUID testCommentId;
  private UUID testLikeId;
  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    logger.info("==== 테스트 시작 ====");
    closeable = MockitoAnnotations.openMocks(this);

    // 테스트 ID 생성
    testUserId = UUID.randomUUID();
    testArticleId = UUID.randomUUID();
    testCommentId = UUID.randomUUID();
    testLikeId = UUID.randomUUID();

    // 테스트 사용자 생성
    testUser = mock(User.class);
    when(testUser.getId()).thenReturn(testUserId);
    when(testUser.getNickname()).thenReturn("테스트유저");
    when(testUser.getEmail()).thenReturn("test@example.com");


    // createMockArticle로 테스트 게시글 생성
    testArticle = createMockArticle();

    // 테스트 댓글 생성
    testComment = mock(Comment.class);
    when(testComment.getId()).thenReturn(testCommentId);
    when(testComment.getContent()).thenReturn("테스트 댓글입니다.");
    when(testComment.getUserNickname()).thenReturn("테스트유저");
    when(testComment.getLikeCount()).thenReturn(0);
    when(testComment.getCreatedAt()).thenReturn(LocalDateTime.now());
    when(testComment.getUpdatedAt()).thenReturn(LocalDateTime.now());
    when(testComment.isDeleted()).thenReturn(false);
    when(testComment.getUser()).thenReturn(testUser);
    when(testComment.getArticle()).thenReturn(testArticle);

    // 테스트 좋아요 생성
    testCommentLike = mock(CommentLike.class);
    when(testCommentLike.getId()).thenReturn(testLikeId);
    when(testCommentLike.getCreatedAt()).thenReturn(LocalDateTime.now());
    when(testCommentLike.getComment()).thenReturn(testComment);
    when(testCommentLike.getUser()).thenReturn(testUser);
    when(testCommentLike.getArticle()).thenReturn(testArticle);

    when(commentMapper.toComment(any(CommentCreateRequest.class), any(User.class), any(Article.class)))
        .thenReturn(testComment);

    //매퍼 모킹
    when(commentMapper.toCommentDto(any(Comment.class), anyBoolean())).thenAnswer(invocation -> {
      Comment comment = invocation.getArgument(0);
      Boolean likedByMe = invocation.getArgument(1);

      if (comment == null) {
        return null;
      }

      return CommentDto.builder()
          .id(comment.getId())
          .content(comment.getContent())
          .articleId(comment.getArticle().getId())
          .articleTitle(comment.getArticle().getTitle())
          .userId(comment.getUser().getId())
          .userNickname(comment.getUserNickname())
          .likeCount(comment.getLikeCount())
          .likedByMe(likedByMe)
          .createdAt(comment.getCreatedAt())
          .build();
    });

    // 기본 모킹 설정
    setupMocks();
  }

  @AfterEach
  void tearDown() throws Exception {
    logger.info("==== 테스트 종료 ====");
    closeable.close();
  }

  /**
   * 테스트용 Article 객체 생성 (mock 객체로 필요한 필드만 설정)
   */
  private Article createMockArticle() {
    Article article = mock(Article.class);

    // 필요한 속성 모킹
    when(article.getId()).thenReturn(testArticleId);
    when(article.getTitle()).thenReturn("테스트 게시글");

    return article;
  }

  private void setupMocks() {
    // User 리포지토리 모킹
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

    // Article 리포지토리 모킹
    when(articleRepository.findById(testArticleId)).thenReturn(Optional.of(testArticle));

    // Comment 리포지토리 모킹
    when(commentRepository.findByIdAndIsDeletedFalse(testCommentId)).thenReturn(Optional.of(testComment));
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
  }

  @Test
  @Order(1)
  @DisplayName("댓글 좋아요 추가 테스트")
  void addLike() {
    // given
    when(commentLikeRepository.findByUserIdAndCommentId(testUserId, testCommentId)).thenReturn(Optional.empty());

    try (var staticCommentLike = mockStatic(CommentLike.class)) { // CommentLike.create static 메서드
      staticCommentLike.when(() -> CommentLike.create(any(Comment.class), any(User.class)))
          .thenReturn(testCommentLike);

      when(commentLikeRepository.save(any(CommentLike.class))).thenReturn(testCommentLike);

      // when
      CommentLikeResponse response = commentLikeService.addLike(testCommentId, testUserId);

      // then
      assertNotNull(response); //빈값아님
      assertEquals(testLikeId, response.getId());
      assertEquals(testUserId, response.getLikedBy());
      assertEquals(testCommentId, response.getCommentId());
      assertNotNull(response.getCreatedAt());

      verify(commentLikeRepository, times(1)).save(any(CommentLike.class));
      verify(testComment, times(1)).incrementLikeCount();
      verify(commentRepository, times(1)).save(testComment);
    }
  }


  @Test
  @Order(2)
  @DisplayName("댓글 좋아요 취소 테스트")
  void removeLike() {
    // given
    when(commentLikeRepository.findByUserIdAndCommentId(testUserId, testCommentId)).thenReturn(Optional.of(testCommentLike));
    doNothing().when(commentLikeRepository).delete(any(CommentLike.class));

    // when
    boolean result = commentLikeService.removeLike(testCommentId, testUserId);

    // then
    assertTrue(result);
    verify(commentLikeRepository, times(1)).delete(any(CommentLike.class));
    verify(testComment, times(1)).decrementLikeCount();
    verify(commentRepository, times(1)).save(testComment);
  }

  @Test
  @Order(3)
  @DisplayName("댓글 좋아요 여부 확인 테스트 - 좋아요 있음")
  void hasLiked_true() {
    // given
    when(commentLikeRepository.findByUserIdAndCommentId(testUserId, testCommentId)).thenReturn(Optional.of(testCommentLike));

    // when
    boolean result = commentLikeService.hasLiked(testCommentId, testUserId);

    // then
    assertTrue(result);
  }

  @Test
  @Order(4)
  @DisplayName("댓글 좋아요 여부 확인 테스트 - 좋아요 없음")
  void hasLiked_false() {
    // given
    when(commentLikeRepository.findByUserIdAndCommentId(testUserId, testCommentId)).thenReturn(Optional.empty());

    // when
    boolean result = commentLikeService.hasLiked(testCommentId, testUserId);

    // then
    assertFalse(result);
  }

  @Test
  @Order(5)
  @DisplayName("댓글 좋아요 정보 조회 테스트")
  void getCommentLike() {
    // given
    when(commentLikeRepository.findByUserIdAndCommentId(testUserId, testCommentId))
        .thenReturn(Optional.of(testCommentLike)); // 좋아요 있음 상태로 설정

    // when
    CommentDto result = commentLikeService.getCommentlike(testCommentId, testUserId);

    // then
    assertNotNull(result);
    assertEquals(testCommentId, result.getId());
    assertEquals(testArticleId, result.getArticleId());
    assertEquals("테스트 게시글", result.getArticleTitle());
    assertEquals(testUserId, result.getUserId());
    assertEquals("테스트유저", result.getUserNickname());
    assertTrue(result.isLikedByMe()); // isLikedByMe는 hasLiked 반영
  }
}