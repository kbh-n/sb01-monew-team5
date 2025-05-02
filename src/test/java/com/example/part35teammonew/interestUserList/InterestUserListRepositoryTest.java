package com.example.part35teammonew.interestUserList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import com.example.part35teammonew.domain.interestUserList.repository.InterestUserListRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

@DataMongoTest
public class InterestUserListRepositoryTest {

  @Autowired
  private InterestUserListRepository repository;

  @Test
  @DisplayName("관심사로 InterestUserList 조회")
  void findByInterest_success() {
    UUID interestId = UUID.randomUUID();
    InterestUserList entity = InterestUserList.setUpNewInterestUserList(interestId);
    repository.save(entity);

    Optional<InterestUserList> result = repository.findByInterest(interestId);

    assertThat(result).isPresent();
    assertThat(result.get().getInterest()).isEqualTo(interestId);
    assertThat(result.get().getUserCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("업데이트 후 재 조회")
  void updateInterestUserList() {
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    InterestUserList entity = InterestUserList.setUpNewInterestUserList(interestId);
    entity.addUser(userId);
    repository.save(entity);

    Optional<InterestUserList> result = repository.findByInterest(interestId);

    assertThat(result).isPresent();
    assertThat(result.get().getUserCount()).isEqualTo(1);
    assertThat(result.get().findUser(userId)).isTrue();
  }

  @Test
  @DisplayName("존재하지 않는 관심사로 조회하면 빈거 반환")
  void findByInterest_fail() {
    Optional<InterestUserList> result = repository.findByInterest(UUID.randomUUID());

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("삭제 후 조회 안 되는지")
  void deleteInterestUserList() {
    UUID interestId = UUID.randomUUID();
    InterestUserList entity = InterestUserList.setUpNewInterestUserList(interestId);
    repository.save(entity);

    repository.delete(entity);

    Optional<InterestUserList> result = repository.findByInterest(interestId);
    assertThat(result).isEmpty();
  }
}
