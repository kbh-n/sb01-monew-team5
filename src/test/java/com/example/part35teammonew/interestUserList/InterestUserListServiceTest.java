package com.example.part35teammonew.interestUserList;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import com.example.part35teammonew.domain.interestUserList.mapper.InterestUserListMapper;
import com.example.part35teammonew.domain.interestUserList.repository.InterestUserListRepository;
import com.example.part35teammonew.domain.interestUserList.service.InterestUserListServiceImp;
import java.util.UUID;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@DataMongoTest
@Import({InterestUserListServiceImp.class, InterestUserListServiceTest.TestMapperConfig.class})
class InterestUserListServiceTest {

  @Autowired
  private InterestUserListServiceImp service;

  @Autowired
  private InterestUserListRepository repository;

  private UUID interestId;
  private UUID userId;

  @BeforeEach
  void setup() {
    interestId = UUID.randomUUID();
    userId = UUID.randomUUID();
  }

  @Test
  @DisplayName("생성확인")
  void createInterestList_success() {
    service.createInterestList(interestId);

    InterestUserList list = repository.findByInterest(interestId).orElse(null);

    assertThat(list).isNotNull();
    assertThat(list.getInterest()).isEqualTo(interestId);
    assertThat(list.getSubscribedUser()).isEmpty();
  }

  @Test
  @DisplayName("유저 추가 확인")
  void addSubscribedUser_success() {
    service.createInterestList(interestId);

    boolean result = service.addSubscribedUser(interestId, userId);

    InterestUserList list = repository.findByInterest(interestId).get();
    assertThat(result).isTrue();
    assertThat(list.getSubscribedUser()).contains(userId);
  }

  @Test
  @DisplayName("유저 제거 확인")
  void subtractSubscribedUser_success() {
    service.createInterestList(interestId);
    service.addSubscribedUser(interestId, userId);

    boolean result = service.subtractSubscribedUser(interestId, userId);

    InterestUserList list = repository.findByInterest(interestId).get();
    assertThat(result).isTrue();
    assertThat(list.getSubscribedUser()).doesNotContain(userId);
  }

  @Test
  @DisplayName("구독자 수 확인")
  void countSubscribedUser_success() {
    service.createInterestList(interestId);
    UUID rand = UUID.randomUUID();
    UUID rand1 = UUID.randomUUID();
    UUID rand2 = UUID.randomUUID();
    service.addSubscribedUser(interestId, userId);
    service.addSubscribedUser(interestId, rand);
    service.addSubscribedUser(interestId, rand1);
    service.addSubscribedUser(interestId, rand2);

    Long count = service.countSubscribedUser(interestId);

    assertThat(count).isEqualTo(4);
  }

  @Test
  @DisplayName("구독자 중복 추가 수 확인")
  void countDuplicateSubscribedUser_success() {
    service.createInterestList(interestId);
    UUID rand = UUID.randomUUID();
    UUID rand1 = UUID.randomUUID();
    service.addSubscribedUser(interestId, userId);
    service.addSubscribedUser(interestId, rand);
    service.addSubscribedUser(interestId, rand1);
    service.addSubscribedUser(interestId, userId);

    Long count = service.countSubscribedUser(interestId);

    assertThat(count).isEqualTo(3);
  }


  @Test
  @DisplayName("관심사 삭제")
  void deleteInterestList_success() {
    service.createInterestList(interestId);
    assertThat(repository.findByInterest(interestId)).isPresent();

    service.deleteInterestList(interestId);
    assertThat(repository.findByInterest(interestId)).isEmpty();
  }

  @Test
  @DisplayName("유저 구독중 체크")
  void checkInterestList_contain_success() {
    service.createInterestList(interestId);
    service.addSubscribedUser(interestId, userId);
    AssertionsForClassTypes.assertThat(service.checkUserSubscribe(interestId, userId)).isTrue();
  }

  @Test
  @DisplayName("유저 안하는지 체크")
  void checkInterestList_contain_fail() {
    service.createInterestList(interestId);
    service.addSubscribedUser(interestId, userId);
    UUID user1 = UUID.randomUUID();
    AssertionsForClassTypes.assertThat(service.checkUserSubscribe(interestId, user1)).isFalse();
  }

  @TestConfiguration
  static class TestMapperConfig {

    @Bean
    public InterestUserListMapper interestUserListMapper() {
      return Mockito.mock(InterestUserListMapper.class);
    }
  }
}