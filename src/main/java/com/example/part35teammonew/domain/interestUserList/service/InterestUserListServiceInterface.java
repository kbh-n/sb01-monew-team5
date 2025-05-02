package com.example.part35teammonew.domain.interestUserList.service;

import com.example.part35teammonew.domain.interestUserList.Dto.InterestUserListDto;
import java.util.List;
import java.util.UUID;

public interface InterestUserListServiceInterface {

  InterestUserListDto createInterestList(UUID interest);//관심사 만들어 질떄 호출 // 해결

  boolean addSubscribedUser(UUID id, UUID user);//유저가 관심사 구독하면 작동

  boolean subtractSubscribedUser(UUID id, UUID user);// 유저가 관심사 구독 취소하면 작동

  Long countSubscribedUser(UUID id); //구독중인 유저수 확인

  void deleteInterestList(UUID interest);// // 뮬리적 삭제

  boolean checkUserSubscribe(UUID interestId, UUID userId);// 유저가 구독 확인중인지

  List<UUID> getAllUserNowSubscribe(UUID interestId);


}
