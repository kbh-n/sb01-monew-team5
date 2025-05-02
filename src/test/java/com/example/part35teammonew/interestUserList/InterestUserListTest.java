package com.example.part35teammonew.interestUserList;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InterestUserListTest {

  private UUID interestId;
  private UUID userId1;
  private UUID userId2;
  private InterestUserList interestUserList;

  @BeforeEach
  void setUp() {
    interestId = UUID.randomUUID();
    userId1 = UUID.randomUUID();
    userId2 = UUID.randomUUID();
    interestUserList = InterestUserList.setUpNewInterestUserList(interestId);
  }


  @Test
  @DisplayName("아무것도 안 추가하면 빔")
  void new_InterestUserList_isEmpty() {
    assertThat(interestUserList.getInterest()).isEqualTo(interestId);
    assertThat(interestUserList.getUserCount()).isEqualTo(0L);
  }

  @Test
  @DisplayName("추가하면 제대로 있음")
  void InterestUserList_get_right_size() {
    interestUserList.addUser(userId1);
    interestUserList.addUser(userId2);

    assertThat(interestUserList.getUserCount()).isEqualTo(2L);
  }

  @Test
  @DisplayName("제거하면 제대로 반영되는지")
  void InterestUserList_minus_newser_count_dicrease() {
    interestUserList.addUser(userId1);
    interestUserList.addUser(userId2);

    interestUserList.subtractUser(userId1);

    assertThat(interestUserList.getUserCount()).isEqualTo(1L);
    assertThat(interestUserList.findUser(userId1)).isFalse();
  }

  @Test
  @DisplayName("추가한거 찾기")
  void find_userByID_if_Add() {
    interestUserList.addUser(userId1);
    assertThat(interestUserList.findUser(userId1)).isTrue();
  }

  @Test
  @DisplayName("안추가한거 못 찾는지")
  void not_exist_user_not_add() {
    assertThat(interestUserList.findUser(UUID.randomUUID())).isFalse();
  }

  @Test
  @DisplayName("중복 추가 막기")
  void duplicationUser_not_add() {
    interestUserList.addUser(userId1);
    interestUserList.addUser(userId1);

    assertThat(interestUserList.getUserCount()).isEqualTo(1L);
  }
}
