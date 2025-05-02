package com.example.part35teammonew.domain.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import com.example.part35teammonew.domain.comment.dto.CommentCreateRequest;
import com.example.part35teammonew.domain.comment.dto.CommentDto;
import com.example.part35teammonew.domain.comment.dto.CommentPageResponse;
import com.example.part35teammonew.domain.comment.dto.CommentUpdateRequest;
import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.comment.mapper.CommentMapper;
import com.example.part35teammonew.domain.comment.repository.CommentLikeRepository;
import com.example.part35teammonew.domain.comment.repository.CommentRepository;
import com.example.part35teammonew.domain.user.entity.User;
import com.example.part35teammonew.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Optional;

import java.util.stream.Collectors;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentServiceTest {

  private static final Logger logger = LoggerFactory.getLogger(CommentServiceTest.class);

  @InjectMocks
  private CommentServiceImpl commentService;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentLikeRepository commentLikeRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ArticleRepository articleRepository;

  @Mock
  private CommentLikeService commentLikeService;

  @Mock
  private CommentMapper commentMapper;

  private User testUser;
  private Article testArticle;
  private Comment testComment;
  private UUID testUserId;
  private UUID testArticleId;
  private UUID testCommentId;
  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    logger.info("==== 테스트 시작 ====");
    closeable = MockitoAnnotations.openMocks(this);

    // 테스트 ID 생성
    testUserId = UUID.randomUUID();
    testArticleId = UUID.randomUUID();
    testCommentId = UUID.randomUUID();


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
    when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

    // Article 리포지토리 모킹
    when(articleRepository.findById(any(UUID.class))).thenReturn(Optional.of(testArticle));
    when(articleRepository.existsById(any(UUID.class))).thenReturn(true);

    // Comment 리포지토리 모킹
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
    when(commentRepository.findByIdAndIsDeletedFalse(any(UUID.class))).thenReturn(Optional.of(testComment));
    when(commentRepository.findById(any(UUID.class))).thenReturn(Optional.of(testComment));
    when(commentRepository.existsById(any(UUID.class))).thenReturn(true);

    // CommentLike 서비스 모킹
    when(commentLikeService.hasLiked(any(UUID.class), any(UUID.class))).thenReturn(false);
  }

  @Test
  @Order(1)
  @DisplayName("댓글 생성 기능 테스트")
  void createComment() {
    // given
    CommentCreateRequest request = new CommentCreateRequest();
    request.setArticleId(testArticleId);
    request.setUserId(testUserId);
    request.setContent("테스트 댓글입니다.");

    // when
    CommentDto result = commentService.createComment(request, testUserId);

    // then
    assertNotNull(result);
    assertEquals("테스트 댓글입니다.", result.getContent());
    assertEquals(testUserId, result.getUserId());
    assertEquals(testArticleId, result.getArticleId());
    assertEquals(testUser.getNickname(), result.getUserNickname());
    assertEquals(0, result.getLikeCount());
    assertFalse(result.isLikedByMe());

    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  @Order(2)
  @DisplayName("댓글 수정 기능 테스트")
  void updateComment() {
    // given
    CommentUpdateRequest request = new CommentUpdateRequest();
    request.setContent("수정된 댓글입니다.");
    doAnswer(invocation -> {
      when(testComment.getContent()).thenReturn("수정된 댓글입니다.");
      return null;
    }).when(testComment).updateContent(anyString());

    // when
    CommentDto result = commentService.updateComment(testCommentId, request, testUserId);

    // then
    assertNotNull(result);
    assertEquals("수정된 댓글입니다.", result.getContent());
    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  @Order(3)
  @DisplayName("댓글 논리 삭제 기능 테스트")
  void deleteComment() {
    // given
    doAnswer(invocation -> {
      when(testComment.isDeleted()).thenReturn(true);
      when(testComment.getUpdatedAt()).thenReturn(LocalDateTime.now());
      return null;
    }).when(testComment).delete();

    // when
    boolean result = commentService.deleteComment(testCommentId, testUserId);

    // then
    assertTrue(result);
    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  @Order(4)
  @DisplayName("댓글 좋아요 수 조회 기능 테스트")
  void countLikes() {
    // given
    when(testComment.getLikeCount()).thenReturn(5);

    // when
    long likeCount = commentService.countLikes(testCommentId);

    // then
    assertEquals(5, likeCount);
  }

  @Test
  @Order(5)
  @DisplayName("댓글 목록 조회 기능 테스트 - 생성일시 기준 내림차순")
  void getComments_orderByCreatedAtDesc() {
    // given
    LocalDateTime now = LocalDateTime.now();

    // 테스트용 댓글 생성
    Comment comment1 = mock(Comment.class);
    when(comment1.getId()).thenReturn(UUID.randomUUID());
    when(comment1.getContent()).thenReturn("첫 번째 댓글입니다.");
    when(comment1.getUserNickname()).thenReturn("테스트유저");
    when(comment1.getLikeCount()).thenReturn(3);
    when(comment1.getCreatedAt()).thenReturn(now.minusHours(2));
    when(comment1.getUpdatedAt()).thenReturn(now.minusHours(2));
    when(comment1.isDeleted()).thenReturn(false);
    when(comment1.getUser()).thenReturn(testUser);
    when(comment1.getArticle()).thenReturn(testArticle);

    Comment comment2 = mock(Comment.class);
    when(comment2.getId()).thenReturn(UUID.randomUUID());
    when(comment2.getContent()).thenReturn("두 번째 댓글입니다.");
    when(comment2.getUserNickname()).thenReturn("테스트유저");
    when(comment2.getLikeCount()).thenReturn(1);
    when(comment2.getCreatedAt()).thenReturn(now.minusHours(1));
    when(comment2.getUpdatedAt()).thenReturn(now.minusHours(1));
    when(comment2.isDeleted()).thenReturn(false);
    when(comment2.getUser()).thenReturn(testUser);
    when(comment2.getArticle()).thenReturn(testArticle);

    Comment comment3 = mock(Comment.class);
    when(comment3.getId()).thenReturn(UUID.randomUUID());
    when(comment3.getContent()).thenReturn("세 번째 댓글입니다.");
    when(comment3.getUserNickname()).thenReturn("테스트유저");
    when(comment3.getLikeCount()).thenReturn(5);
    when(comment3.getCreatedAt()).thenReturn(now);
    when(comment3.getUpdatedAt()).thenReturn(now);
    when(comment3.isDeleted()).thenReturn(false);
    when(comment3.getUser()).thenReturn(testUser);
    when(comment3.getArticle()).thenReturn(testArticle);

    // 정렬된 댓글 목록 설정 (생성일 내림차순)
    List<Comment> sortedComments = new ArrayList<>();
    sortedComments.add(comment3); // 최신 댓글
    sortedComments.add(comment2);
    sortedComments.add(comment1); // 초기 댓글

    // after가 null이고 cursor가 null인 경우 (초기 호출)
    when(commentRepository.findByArticleIdCreatedAtDesc(
        eq(testArticleId),
        any(Pageable.class)))
        .thenReturn(sortedComments);

    // 댓글 개수 모킹
    when(commentRepository.countByArticleIdAndIsDeletedFalse(eq(testArticleId)))
        .thenReturn(3L);

    // DTO 변환 모킹
    List<CommentDto> commentDtos = new ArrayList<>();
    for (Comment comment : sortedComments) {
      CommentDto dto = CommentDto.builder()
          .id(comment.getId())
          .content(comment.getContent())
          .articleId(testArticleId)
          .articleTitle("테스트 게시글")
          .userId(testUserId)
          .userNickname(comment.getUserNickname())
          .likeCount(comment.getLikeCount())
          .likedByMe(false)
          .createdAt(comment.getCreatedAt())
          .build();

      commentDtos.add(dto);  // 명시적으로 DTO 목록에 추가

      // Comment -> DTO 매핑 모킹
      when(commentMapper.toCommentDto(eq(comment), eq(false))).thenReturn(dto);
      when(commentMapper.toCommentDto(eq(comment), eq(true))).thenReturn(dto);
    }

    // when - 서비스 호출
    CommentPageResponse response = commentService.getComments(
        testArticleId,
        "createdAt",
        "DESC",
        null,
        null,
        10,
        testUserId
    );

    // then
    assertNotNull(response);
    assertEquals(3, response.getContent().size());
    assertEquals("세 번째 댓글입니다.", response.getContent().get(0).getContent());
    assertEquals("두 번째 댓글입니다.", response.getContent().get(1).getContent());
    assertEquals("첫 번째 댓글입니다.", response.getContent().get(2).getContent());
    assertNotNull(response.getNextCursor());
    assertEquals(3, response.getTotalElements());
  }

  @Test
  @Order(6)
  @DisplayName("댓글 목록 조회 기능 테스트 - 좋아요 수 기준 내림차순")
  void getComments_orderByLikesDesc() {
    // given
    LocalDateTime now = LocalDateTime.now();

    // 테스트용 댓글 생성
    Comment comment1 = mock(Comment.class);
    when(comment1.getId()).thenReturn(UUID.randomUUID());
    when(comment1.getContent()).thenReturn("첫 번째 댓글입니다.");
    when(comment1.getUserNickname()).thenReturn("테스트유저");
    when(comment1.getLikeCount()).thenReturn(3);
    when(comment1.getCreatedAt()).thenReturn(now.minusHours(2));
    when(comment1.getUpdatedAt()).thenReturn(now.minusHours(2));
    when(comment1.isDeleted()).thenReturn(false);
    when(comment1.getUser()).thenReturn(testUser);
    when(comment1.getArticle()).thenReturn(testArticle);

    Comment comment2 = mock(Comment.class);
    when(comment2.getId()).thenReturn(UUID.randomUUID());
    when(comment2.getContent()).thenReturn("두 번째 댓글입니다.");
    when(comment2.getUserNickname()).thenReturn("테스트유저");
    when(comment2.getLikeCount()).thenReturn(1);
    when(comment2.getCreatedAt()).thenReturn(now.minusHours(1));
    when(comment2.getUpdatedAt()).thenReturn(now.minusHours(1));
    when(comment2.isDeleted()).thenReturn(false);
    when(comment2.getUser()).thenReturn(testUser);
    when(comment2.getArticle()).thenReturn(testArticle);

    Comment comment3 = mock(Comment.class);
    when(comment3.getId()).thenReturn(UUID.randomUUID());
    when(comment3.getContent()).thenReturn("세 번째 댓글입니다.");
    when(comment3.getUserNickname()).thenReturn("테스트유저");
    when(comment3.getLikeCount()).thenReturn(5);
    when(comment3.getCreatedAt()).thenReturn(now);
    when(comment3.getUpdatedAt()).thenReturn(now);
    when(comment3.isDeleted()).thenReturn(false);
    when(comment3.getUser()).thenReturn(testUser);
    when(comment3.getArticle()).thenReturn(testArticle);

    // 정렬된 댓글 목록 설정 (좋아요 수 내림차순)
    List<Comment> sortedComments = new ArrayList<>();
    sortedComments.add(comment3); // 좋아요 5개
    sortedComments.add(comment1); // 좋아요 3개
    sortedComments.add(comment2); // 좋아요 1개

    // 댓글 개수 모킹
    when(commentRepository.countByArticleIdAndIsDeletedFalse(eq(testArticleId)))
        .thenReturn(3L);

    // 커서 기반 API 모킹
    when(commentRepository.findByArticleIdLikesDesc(
        eq(testArticleId),
        any(Pageable.class)))
        .thenReturn(sortedComments);

    // DTO 변환 모킹
    for (Comment comment : sortedComments) {
      CommentDto dto = CommentDto.builder()
          .id(comment.getId())
          .content(comment.getContent())
          .articleId(testArticleId)
          .articleTitle("테스트 게시글")
          .userId(testUserId)
          .userNickname(comment.getUserNickname())
          .likeCount(comment.getLikeCount())
          .likedByMe(false)
          .createdAt(comment.getCreatedAt())
          .build();

      // Comment -> DTO 매핑 모킹
      when(commentMapper.toCommentDto(eq(comment), eq(false))).thenReturn(dto);
    }

    // when
    CommentPageResponse response = commentService.getComments(
        testArticleId,
        "likes",
        "DESC",
        null,
        null,
        10,
        testUserId
    );

    // then
    assertNotNull(response);
    assertEquals(3, response.getContent().size());
    assertEquals("세 번째 댓글입니다.", response.getContent().get(0).getContent()); // 좋아요 5개
    assertEquals("첫 번째 댓글입니다.", response.getContent().get(1).getContent()); // 좋아요 3개
    assertEquals("두 번째 댓글입니다.", response.getContent().get(2).getContent()); // 좋아요 1개
    assertNotNull(response.getNextCursor());
    assertEquals(3, response.getTotalElements());
  }

  @Test
  @Order(7)
  @DisplayName("개별 댓글 조회 기능 테스트")
  void getComment() {
    // given
    when(testComment.getContent()).thenReturn("조회할 댓글입니다.");
    when(testComment.getLikeCount()).thenReturn(10);

    // when
    CommentDto result = commentService.getComment(testCommentId, testUserId);

    // then
    assertNotNull(result);
    assertEquals("조회할 댓글입니다.", result.getContent());
    assertEquals(10, result.getLikeCount());
    assertEquals(testUserId, result.getUserId());
    assertEquals(testArticleId, result.getArticleId());
  }

  @Test
  @Order(8)
  @DisplayName("댓글에 좋아요 여부 확인 기능 테스트")
  void getComment_withLikeStatus() {
    // given
    when(testComment.getContent()).thenReturn("좋아요 상태 테스트용 댓글입니다.");
    when(testComment.getLikeCount()).thenReturn(1);

    when(commentLikeService.hasLiked(eq(testCommentId), eq(testUserId))).thenReturn(true);

    // when
    CommentDto result = commentService.getComment(testCommentId, testUserId);

    // then
    assertNotNull(result);
    assertEquals(1, result.getLikeCount());
    assertTrue(result.isLikedByMe());
  }
}