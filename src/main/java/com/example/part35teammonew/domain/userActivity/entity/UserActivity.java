package com.example.part35teammonew.domain.userActivity.entity;

import com.example.part35teammonew.domain.userActivity.Dto.ArticleInfoView;
import com.example.part35teammonew.domain.userActivity.Dto.InterestView;
import com.example.part35teammonew.domain.userActivity.Dto.LikeCommentView;
import com.example.part35teammonew.domain.userActivity.Dto.RecentCommentView;
import com.example.part35teammonew.domain.userActivity.Dto.UserInfoDto;
import com.example.part35teammonew.exeception.AlreadySubscribedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "UserActivity")
@Getter
public class UserActivity {

  @Id
  private ObjectId id;
  @Indexed
  private final UUID userId; //이거로 인덱스
  private String nickName; //애는 변경 됨
  private String email; //일단 병경 되는 기능은 없지만 혹시 모르니 final로 안함
  private final LocalDateTime createdAt;
  private Set<InterestView> subscriptions; //이거 포함 밑에 세개는 final하면 테스트가 안됨
  private LinkedList<RecentCommentView> recentcomments;
  private LinkedList<LikeCommentView> likeComment;
  private LinkedList<ArticleInfoView> articleViews;

  @Builder
  private UserActivity(LocalDateTime createdAt, UUID userId, String nickName, String email) {
    this.userId = userId;
    this.nickName = nickName;
    this.email = email;
    this.createdAt = createdAt;
    this.subscriptions = new HashSet<>();
    this.recentcomments = new LinkedList<>();
    this.likeComment = new LinkedList<>();
    this.articleViews = new LinkedList<>();
  }

  public static UserActivity setUpNewUserActivity(LocalDateTime createdAt, UUID userId, String nickName,
      String email) {
    return UserActivity.builder()
        .createdAt(createdAt)
        .userId(userId)
        .nickName(nickName)
        .email(email)
        .build();
  }

  public void updateSubscriptions(InterestView interest) {
    if (!subscriptions.contains(interest)) {
      subscriptions.add(interest);
    } else {
      throw new AlreadySubscribedException("이미 구독한 관심사입니다: " + interest.getInterestName());
    }
  }

  public void subtractSubscriptions(InterestView interest) {
    if (subscriptions.contains(interest)) {
      subscriptions.remove(interest);
    } else {
      throw new AlreadySubscribedException("구독되지 않은 관심사입니다.: " + interest.getInterestName());
    }
  }

  public void updateComments(RecentCommentView comment) {

    if (recentcomments.size() >= 10) {
      recentcomments.poll();
    }
    recentcomments.add(comment);
  }

  public void updateCommentLikes(LikeCommentView comment) {
    if (likeComment.stream().anyMatch(existing -> existing.getId().equals(comment.getId()))) {
      return;
    }
    if (likeComment.size() >= 10) {
      likeComment.poll();
    }
    likeComment.add(comment);
  }

  public void updateArticleViews(ArticleInfoView article) {
    if(articleViews.stream().anyMatch(existing -> existing.getId().equals(article.getArticleId()))){
      return;
    }
    if (articleViews.size() >= 10) {
      articleViews.poll();
    }
    articleViews.add(article);
  }

  public void updateUserInfo(UserInfoDto userInfoDto) {
    this.nickName = userInfoDto.getNickName();
  }

}