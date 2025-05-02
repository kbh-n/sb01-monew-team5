package com.example.part35teammonew.domain.userActivity.Dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserActivityDto {

  //private ObjectId id;
  private UUID userId;
  private String email;
  private String nickname;
  private LocalDateTime createdAt;
  private Set<InterestView> subscriptions;
  private LinkedList<RecentCommentView> comments;
  private LinkedList<LikeCommentView> commentLikes;
  private LinkedList<ArticleInfoView> articleViews;
}
