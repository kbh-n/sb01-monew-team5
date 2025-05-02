package com.example.part35teammonew.domain.userActivity.service;

import com.example.part35teammonew.domain.userActivity.Dto.ArticleInfoView;
import com.example.part35teammonew.domain.userActivity.Dto.InterestView;
import com.example.part35teammonew.domain.userActivity.Dto.LikeCommentView;
import com.example.part35teammonew.domain.userActivity.Dto.RecentCommentView;
import com.example.part35teammonew.domain.userActivity.Dto.UserActivityDto;
import com.example.part35teammonew.domain.userActivity.Dto.UserInfoDto;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public interface UserActivityServiceInterface {

  UserActivityDto createUserActivity(LocalDateTime createdAt, UUID userId, String nickName,
      String email); //유저 만들어질떄

  UserActivityDto getUserActivity(UUID id);


  //유저가 관심사 구독 할떄
  void addInterestView(UUID id, InterestView interestView);//해결

  //유저가 관심사 구독 취소 할떄
  void subtractInterestView(UUID id, InterestView interestView); //해결

  //유저 정보 바뀔떄마다
  UserActivityDto updateUserInformation(UUID id, UserInfoDto userInfoDto); //해결

  //유조가 댓글 달떄마다
  void addRecentCommentView(UUID id, RecentCommentView recentCommentView);//해결

  //유저가 좋아요 누를떄마다
  void addLikeCommentView(UUID id, LikeCommentView likeCommentView);//해결

  //유저가 기사 볼떄마다
  void addArticleInfoView(UUID id, ArticleInfoView articleInfoView); // 해결

  void deleteUserActivity(UUID id); //물리 삭제  ,해결


}
