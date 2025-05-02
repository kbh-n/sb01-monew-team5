package com.example.part35teammonew.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.part35teammonew.domain.article.entity.Article;
import com.example.part35teammonew.domain.article.repository.ArticleRepository;
import com.example.part35teammonew.domain.comment.dto.CommentDto;
import com.example.part35teammonew.domain.comment.dto.CommentPageResponse;
import com.example.part35teammonew.domain.comment.entity.Comment;
import com.example.part35teammonew.domain.comment.mapper.CommentMapper;
import com.example.part35teammonew.domain.comment.repository.CommentRepository;
import com.example.part35teammonew.domain.user.entity.User;
import com.example.part35teammonew.exeception.comment.CommentNotFound;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommentPageTest {

  private static final Logger log = LoggerFactory.getLogger(CommentPageTest.class);

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private ArticleRepository articleRepository;

  @Mock
  private CommentLikeService commentLikeService;

  @Mock
  private CommentMapper commentMapper;

  @InjectMocks
  private CommentServiceImpl commentService;

  private UUID articleId;
  private UUID userId;
  private List<Comment> comments;
  private List<CommentDto> commentDtos;

  // Comment와 CommentDto를 생성하는 헬퍼 메서드
  private Comment createMockComment(int index, UUID articleId) {
    int commentNumber = index + 1;
    Comment comment = mock(Comment.class);
    UUID commentId = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.now().minusDays(100 - commentNumber);

    // 필요한 메서드만 모킹
    lenient().when(comment.getId()).thenReturn(commentId);
    lenient().when(comment.getContent()).thenReturn("댓글 내용 " + commentNumber);
    lenient().when(comment.getLikeCount()).thenReturn(100 - index);
    lenient().when(comment.getCreatedAt()).thenReturn(createdAt);
    lenient().when(comment.getUpdatedAt()).thenReturn(createdAt);
    lenient().when(comment.isDeleted()).thenReturn(false);

    // 게시글 모킹
    Article article = mock(Article.class);
    lenient().when(article.getId()).thenReturn(articleId);
    lenient().when(article.getTitle()).thenReturn("테스트 기사");
    lenient().when(comment.getArticle()).thenReturn(article);

    // 사용자 모킹
    User user = mock(User.class);
    lenient().when(user.getId()).thenReturn(userId);
    lenient().when(comment.getUser()).thenReturn(user);
    lenient().when(comment.getUserNickname()).thenReturn("테스트 유저");

    return comment;
  }

  private CommentDto createCommentDto(Comment comment, UUID userId) {
    return CommentDto.builder()
        .id(comment.getId())
        .articleId(comment.getArticle().getId())
        .articleTitle("테스트 기사")
        .userId(comment.getUser().getId())
        .userNickname(comment.getUserNickname())
        .content(comment.getContent())
        .likeCount(comment.getLikeCount())
        .likedByMe(false)
        .createdAt(comment.getCreatedAt())
        .build();
  }

  @BeforeEach
  void setUp() {
    // 테스트 데이터 준비
    articleId = UUID.randomUUID();
    userId = UUID.randomUUID();

    // 100개의 댓글 생성 (테스트용)
    comments = new ArrayList<>();
    commentDtos = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      Comment comment = createMockComment(i, articleId);
      comments.add(comment);

      CommentDto dto = createCommentDto(comment, userId);
      commentDtos.add(dto);
    }
    log.info("테스트 데이터 준비: 총 100개 댓글 (댓글 내용 1 ~ 댓글 내용 100), 좋아요 설정: comments[0].likeCount = 100, comments[99].likeCount = 1");

    // ArticleRepository Mock 설정
    when(articleRepository.existsById(articleId)).thenReturn(true);

    // CommentLikeService Mock 설정
    when(commentLikeService.hasLiked(any(UUID.class), eq(userId))).thenReturn(false);

    // CommentRepository 전체 카운트 Mock 설정
    when(commentRepository.countByArticleIdAndIsDeletedFalse(articleId)).thenReturn(100L);
  }

  /**
   * 댓글 목록을 정렬 기준과 방향에 따라 정렬하는 헬퍼 메서드
   */
  private List<Comment> orderComments(List<Comment> commentList, String orderBy, String direction) {
    List<Comment> orderedList = new ArrayList<>(commentList);

    // 정렬 전 상태 출력
    int firstIndex;
    int lastIndex;

    if (commentList.size() > 0) {
      firstIndex = Integer.parseInt(commentList.get(0).getContent().replace("댓글 내용 ", ""));
      lastIndex = Integer.parseInt(commentList.get(commentList.size()-1).getContent().replace("댓글 내용 ", ""));
    } else {
      firstIndex = 0;
      lastIndex = 0;
    }

    log.info("정렬 전 범위: 댓글 내용 {} ~ 댓글 내용 {}", firstIndex, lastIndex);

    // 정렬 기준이 생성일시인 경우
    if (orderBy.equalsIgnoreCase("createdAt")) {
      if (direction.equalsIgnoreCase("DESC")) {
        // 내림차순 정렬 (최신순 -> 오래된 순)
        orderedList.sort((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt())); //댓글1 댓글2 비교
      } else {
        // 오름차순 정렬 (오래된순 -> 최신순)
        orderedList.sort((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt()));
      }
    }
    // 정렬 기준이 좋아요 수인 경우
    else if (orderBy.equalsIgnoreCase("likes") || orderBy.equalsIgnoreCase("likeCount")) {
      if (direction.equalsIgnoreCase("DESC")) {
        // 내림차순 정렬 (좋아요 많은 순 -> 적은 순)
        orderedList.sort((c1, c2) -> c2.getLikeCount().compareTo(c1.getLikeCount())); //댓글1 댓글2 비교
      } else {
        // 오름차순 정렬 (좋아요 적은 순 -> 많은 순)
        orderedList.sort((c1, c2) -> c1.getLikeCount().compareTo(c2.getLikeCount()));
      }
    }

    // 정렬 후 상태 출력
    int firstIdxAfter;
    int lastIdxAfter;

    if (orderedList.size() > 0) {
      firstIdxAfter = Integer.parseInt(orderedList.get(0).getContent().replace("댓글 내용 ", ""));
      lastIdxAfter = Integer.parseInt(orderedList.get(orderedList.size()-1).getContent().replace("댓글 내용 ", ""));
    } else {
      firstIdxAfter = 0;
      lastIdxAfter = 0;
    }

    log.info("정렬 후({} {}) 범위: 댓글 내용 {} ~ 댓글 내용 {}", orderBy, direction, firstIdxAfter, lastIdxAfter);

    return orderedList;
  }

// 생성일시 기준 ========================================================================
  @Test
  @Order(1)
  @DisplayName("첫 번째 페이지 조회 - 생성일시 기준 내림차순 (최신순)")
  void getFirstPageByCreatedAtDesc() {
    log.info("=== 테스트: 첫 번째 페이지 (최신순) ===");
    // Given
    int limit = 30;
    String orderBy = "createdAt";
    String direction = "DESC";

    // 인덱스 70-99 댓글을 최신순으로 정렬 (리미트 30이라서)
    List<Comment> pageComments = orderComments(
        comments.subList(70, 100), orderBy, direction);

    // 필요한 댓글에 대해서만 매퍼 모킹
    for (Comment comment : pageComments) {
      CommentDto dto = commentDtos.stream()
          .filter(d -> d.getId().equals(comment.getId()))
          .findFirst()
          .orElseThrow();
      when(commentMapper.toCommentDto(eq(comment), anyBoolean())).thenReturn(dto);
    }

    // CommentRepository Mock 설정
    when(commentRepository.findByArticleIdCreatedAtDesc(
        eq(articleId), any(Pageable.class)))
        .thenReturn(pageComments);

    // When
    CommentPageResponse response = commentService.getComments(
        articleId, orderBy, direction, null, null, limit, userId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(30);

    // 핵심 검증
    log.info("[첫 페이지 결과] 예상: 댓글 내용 100 ~ 댓글 내용 71, 실제: {} ~ {}",
        response.getContent().get(0).getContent(),
        response.getContent().get(29).getContent());
    assertThat(response.getContent().get(0).getContent()).isEqualTo("댓글 내용 100");
    assertThat(response.getContent().get(29).getContent()).isEqualTo("댓글 내용 71");
  }

  @Test
  @Order(2)
  @DisplayName("두 번째 페이지 조회 - 생성일시 기준 내림차순 (최신순)")
  void getSecondPageByCreatedAtDesc() {
    log.info("=== 테스트: 두 번째 페이지 (최신순) ===");
    // Given
    int limit = 30;
    String orderBy = "createdAt";
    String direction = "DESC";

    // 두 번째 페이지 범위(40-69)의 댓글을 최신순으로 정렬
    List<Comment> pageComments = orderComments(
        comments.subList(40, 70), orderBy, direction);

    // 커서 댓글 정보
    UUID cursorId = comments.get(70).getId();
    LocalDateTime cursorDate = comments.get(70).getCreatedAt();
    log.info("커서 댓글: 댓글 내용 71 (인덱스 70)");

    // 필요한 댓글에 대해서만 매퍼 모킹
    for (Comment comment : pageComments) {
      CommentDto dto = commentDtos.stream()
          .filter(d -> d.getId().equals(comment.getId()))
          .findFirst()
          .orElseThrow();
      when(commentMapper.toCommentDto(eq(comment), anyBoolean())).thenReturn(dto);
    }

    // 커서 댓글 찾기 모킹
    when(commentRepository.findByIdAndIsDeletedFalse(cursorId))
        .thenReturn(Optional.of(comments.get(70)));

    // CommentRepository Mock 설정
    when(commentRepository.findByArticleIdCreatedAtBeforeCursorWithValues(
        eq(articleId), eq(cursorId), eq(cursorDate), any(Pageable.class)))
        .thenReturn(pageComments);

    // When
    CommentPageResponse response = commentService.getComments(
        articleId, orderBy, direction, cursorId.toString(), cursorDate, limit, userId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(30);

    // 핵심 검증
    log.info("[두 번째 페이지 결과] 예상: 댓글 내용 70 ~ 댓글 내용 41, 실제: {} ~ {}",
        response.getContent().get(0).getContent(),
        response.getContent().get(29).getContent());
    assertThat(response.getContent().get(0).getContent()).isEqualTo("댓글 내용 70");
    assertThat(response.getContent().get(29).getContent()).isEqualTo("댓글 내용 41");
  }

  @Test
  @Order(3)
  @DisplayName("세 번째 페이지 조회 - 생성일시 기준 내림차순 (최신순)")
  void getThirdPageByCreatedAtDesc() {
    log.info("=== 테스트: 세 번째 페이지 (최신순) ===");
    // Given
    int limit = 30;
    String orderBy = "createdAt";
    String direction = "DESC";

    // 세 번째 페이지 범위(10-39)의 댓글을 최신순으로 정렬
    List<Comment> pageComments = orderComments(
        comments.subList(10, 40), orderBy, direction);

    // 커서 댓글 정보
    UUID cursorId = comments.get(40).getId();
    LocalDateTime cursorDate = comments.get(40).getCreatedAt();
    log.info("커서 댓글: 댓글 내용 41 (인덱스 40)");

    // 필요한 댓글에 대해서만 매퍼 모킹
    for (Comment comment : pageComments) {
      CommentDto dto = commentDtos.stream()
          .filter(d -> d.getId().equals(comment.getId()))
          .findFirst()
          .orElseThrow();
      when(commentMapper.toCommentDto(eq(comment), anyBoolean())).thenReturn(dto);
    }

    // 커서 댓글 찾기 모킹
    when(commentRepository.findByIdAndIsDeletedFalse(cursorId))
        .thenReturn(Optional.of(comments.get(40)));

    // CommentRepository Mock 설정
    when(commentRepository.findByArticleIdCreatedAtBeforeCursorWithValues(
        eq(articleId), eq(cursorId), eq(cursorDate), any(Pageable.class)))
        .thenReturn(pageComments);

    // When
    CommentPageResponse response = commentService.getComments(
        articleId, orderBy, direction, cursorId.toString(), cursorDate, limit, userId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(30);

    // 핵심 검증
    log.info("[세 번째 페이지 결과] 예상: 댓글 내용 40 ~ 댓글 내용 11, 실제: {} ~ {}",
        response.getContent().get(0).getContent(),
        response.getContent().get(29).getContent());
    assertThat(response.getContent().get(0).getContent()).isEqualTo("댓글 내용 40");
    assertThat(response.getContent().get(29).getContent()).isEqualTo("댓글 내용 11");
  }

  @Test
  @Order(4)
  @DisplayName("마지막 페이지 조회 (10개) - 생성일시 기준 내림차순 (최신순)")
  void getLastPageByCreatedAtDesc() {
    log.info("=== 테스트: 마지막 페이지 (최신순) ===");
    // Given
    int limit = 30;
    String orderBy = "createdAt";
    String direction = "DESC";

    // 마지막 페이지 범위(0-9)의 댓글을 최신순으로 정렬
    List<Comment> pageComments = orderComments(
        comments.subList(0, 10), orderBy, direction);

    // 커서 댓글 정보
    UUID cursorId = comments.get(10).getId();
    LocalDateTime cursorDate = comments.get(10).getCreatedAt();
    log.info("커서 댓글: 댓글 내용 11 (인덱스 10)");

    // 필요한 댓글에 대해서만 매퍼 모킹
    for (Comment comment : pageComments) {
      CommentDto dto = commentDtos.stream()
          .filter(d -> d.getId().equals(comment.getId()))
          .findFirst()
          .orElseThrow();
      when(commentMapper.toCommentDto(eq(comment), anyBoolean())).thenReturn(dto);
    }

    // 커서 댓글 찾기 모킹
    when(commentRepository.findByIdAndIsDeletedFalse(cursorId))
        .thenReturn(Optional.of(comments.get(10)));

    // CommentRepository Mock 설정
    when(commentRepository.findByArticleIdCreatedAtBeforeCursorWithValues(
        eq(articleId), eq(cursorId), eq(cursorDate), any(Pageable.class)))
        .thenReturn(pageComments);

    // When
    CommentPageResponse response = commentService.getComments(
        articleId, orderBy, direction, cursorId.toString(), cursorDate, limit, userId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(10); // 마지막 페이지는 10개만 있음

    // 핵심 검증
    log.info("[마지막 페이지 결과] 예상: 댓글 내용 10 ~ 댓글 내용 1, 실제: {} ~ {}",
        response.getContent().get(0).getContent(),
        response.getContent().get(9).getContent());
    log.info("더 이상 페이지 없음: {}", !response.isHasNext());
    assertThat(response.getContent().get(0).getContent()).isEqualTo("댓글 내용 10");
    assertThat(response.getContent().get(9).getContent()).isEqualTo("댓글 내용 1");
    assertThat(response.isHasNext()).isFalse(); // 더 이상 데이터 없음 확인
  }
// 좋아요 기준 ========================================================================
  @Test
  @Order(5)
  @DisplayName("좋아요 기준 정렬 - 내림차순")
  void getPageByLikesDesc() {
    log.info("=== 테스트: 좋아요 기준 내림차순 ===");
    // Given
    int limit = 30;
    String orderBy = "likes";
    String direction = "DESC";

    // 인덱스 0-29의 댓글을 좋아요 내림차순으로 정렬
    List<Comment> pageComments = orderComments(
        comments.subList(0, 30), orderBy, direction);

    // 필요한 댓글에 대해서만 매퍼 모킹
    for (Comment comment : pageComments) {
      CommentDto dto = commentDtos.stream()
          .filter(d -> d.getId().equals(comment.getId()))
          .findFirst()
          .orElseThrow();
      when(commentMapper.toCommentDto(eq(comment), anyBoolean())).thenReturn(dto);
    }

    // CommentRepository Mock 설정
    when(commentRepository.findByArticleIdLikesDesc(
        eq(articleId), any(Pageable.class)))
        .thenReturn(pageComments);

    // When
    CommentPageResponse response = commentService.getComments(
        articleId, orderBy, direction, null, null, limit, userId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(30);

    // 핵심 검증
    log.info("[좋아요 내림차순 결과] 예상 좋아요 수: 100 ~ 71, 실제: {} ~ {}",
        response.getContent().get(0).getLikeCount(),
        response.getContent().get(29).getLikeCount());
    assertThat(response.getContent().get(0).getLikeCount()).isEqualTo(100);
    assertThat(response.getContent().get(29).getLikeCount()).isEqualTo(71);
  }

  @Test
  @Order(6)
  @DisplayName("생성일시 기준 정렬 - 오름차순 (오래된 순)")
  void getPageByCreatedAtAsc() {
    log.info("=== 테스트: 생성일시 기준 오름차순 (오래된 순) ===");
    // Given
    int limit = 30;
    String orderBy = "createdAt";
    String direction = "ASC";

    // 인덱스 0-29의 댓글을 오래된순으로 정렬
    List<Comment> pageComments = orderComments(
        comments.subList(0, 30), orderBy, direction);

    // 필요한 댓글에 대해서만 매퍼 모킹
    for (Comment comment : pageComments) {
      CommentDto dto = commentDtos.stream()
          .filter(d -> d.getId().equals(comment.getId()))
          .findFirst()
          .orElseThrow();
      when(commentMapper.toCommentDto(eq(comment), anyBoolean())).thenReturn(dto);
    }

    // CommentRepository Mock 설정
    when(commentRepository.findByArticleIdCreatedAtAsc(
        eq(articleId), any(Pageable.class)))
        .thenReturn(pageComments);

    // When
    CommentPageResponse response = commentService.getComments(
        articleId, orderBy, direction, null, null, limit, userId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(30);

    // 핵심 검증
    log.info("[오래된순 결과] 예상: 댓글 내용 1 ~ 댓글 내용 30, 실제: {} ~ {}",
        response.getContent().get(0).getContent(),
        response.getContent().get(29).getContent());
    assertThat(response.getContent().get(0).getContent()).isEqualTo("댓글 내용 1");
    assertThat(response.getContent().get(29).getContent()).isEqualTo("댓글 내용 30");
  }

  @Test
  @Order(7)
  @DisplayName("좋아요 기준 정렬 - 오름차순 (좋아요 적은 순)")
  void getPageByLikesAsc() {
    log.info("=== 테스트: 좋아요 기준 오름차순 (좋아요 적은 순) ===");
    // Given
    int limit = 30;
    String orderBy = "likes";
    String direction = "ASC";

    // 인덱스 70-99의 댓글을 좋아요 오름차순으로 정렬
    List<Comment> pageComments = orderComments(
        comments.subList(70, 100), orderBy, direction);

    // 필요한 댓글에 대해서만 매퍼 모킹
    for (Comment comment : pageComments) {
      CommentDto dto = commentDtos.stream()
          .filter(d -> d.getId().equals(comment.getId()))
          .findFirst()
          .orElseThrow();
      when(commentMapper.toCommentDto(eq(comment), anyBoolean())).thenReturn(dto);
    }

    // CommentRepository Mock 설정
    when(commentRepository.findByArticleIdLikesAsc(
        eq(articleId), any(Pageable.class)))
        .thenReturn(pageComments);

    // When
    CommentPageResponse response = commentService.getComments(
        articleId, orderBy, direction, null, null, limit, userId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(30);

    // 핵심 검증
    log.info("[좋아요 오름차순 결과] 예상 좋아요 수: 1 ~ 30, 실제: {} ~ {}",
        response.getContent().get(0).getLikeCount(),
        response.getContent().get(29).getLikeCount());
    assertThat(response.getContent().get(0).getLikeCount()).isEqualTo(1);
    assertThat(response.getContent().get(29).getLikeCount()).isEqualTo(30);
  }

}