package com.example.part35teammonew.domain.interestUserList.service;

import static com.example.part35teammonew.domain.interestUserList.entity.InterestUserList.setUpNewInterestUserList;

import com.example.part35teammonew.domain.interestUserList.Dto.InterestUserListDto;
import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import com.example.part35teammonew.domain.interestUserList.mapper.InterestUserListMapper;
import com.example.part35teammonew.domain.interestUserList.repository.InterestUserListRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterestUserListServiceImp implements InterestUserListServiceInterface {


  private final InterestUserListRepository interestUserListRepository;
  private final InterestUserListMapper interestUserListMapper;

  InterestUserListServiceImp(
      @Autowired InterestUserListRepository interestUserListRepository,
      @Autowired InterestUserListMapper interestUserListMapper
  ) {
    this.interestUserListRepository = interestUserListRepository;
    this.interestUserListMapper = interestUserListMapper;
  }

  @Override
  @Transactional//관심사 만들어 질떄 호출
  public InterestUserListDto createInterestList(UUID interest) {
    InterestUserList interestUserList = setUpNewInterestUserList(interest);
    interestUserListRepository.save(interestUserList);
    return interestUserListMapper.toDto(interestUserList);
  }

  @Override
  @Transactional//유저가 관심사 구독하면 작동
  public boolean addSubscribedUser(UUID interestId, UUID userId) {
    return interestUserListRepository.findByInterest(interestId)
        .map(interestUserList -> {
          interestUserList.addUser(userId);
          interestUserListRepository.save(interestUserList);
          return true;
        })
        .orElse(false);
  }

  @Override
  @Transactional // 유저가 관심사 구독 취소하면 작동
  public boolean subtractSubscribedUser(UUID id, UUID user) {
    return interestUserListRepository.findByInterest(id)
        .map(interestUserList -> {
          interestUserList.subtractUser(user);
          interestUserListRepository.save(interestUserList);
          return true;
        })
        .orElse(false);
  }

  @Override
  @Transactional(readOnly = true) //유저가 구독중인지 확인
  public boolean checkUserSubscribe(UUID interestId, UUID userId) {
    return interestUserListRepository.findByInterest(interestId)
        .map(interestUserList -> {
          return interestUserList.findUser(userId);
        })
        .orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UUID> getAllUserNowSubscribe(UUID interestId) {
    return interestUserListRepository.findByInterest(interestId)
        .map(interestUserList -> interestUserList.getSubscribedUser().stream().toList())
        .orElse(List.of());
  }

  @Override
  @Transactional(readOnly = true) // 관심사 구독중인 사람 수 체크
  public Long countSubscribedUser(UUID interest) {
    return interestUserListRepository.findByInterest(interest)
        .map(InterestUserList::getUserCount)
        .orElse(0L);
  }

  @Override
  @Transactional //관심사 물리적 삭제시
  public void deleteInterestList(UUID interest) {
    interestUserListRepository.deleteByInterest(interest);

  }
}
