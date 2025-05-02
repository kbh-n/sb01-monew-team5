package com.example.part35teammonew.domain.interest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import com.example.part35teammonew.domain.interestUserList.repository.InterestUserListRepository;

@ExtendWith(MockitoExtension.class)
class IsSubscribedTest {

	@Mock
	private InterestUserListRepository userListRepository;

	@InjectMocks
	private InterestServiceImpl interestService;

	private UUID interestId;
	private UUID userId;

	@BeforeEach
	void setUp() {
		interestId = UUID.randomUUID();
		userId = UUID.randomUUID();
	}

	@Test
	@DisplayName("isSubscribed: 구독 중인 경우 true 반환")
	void isSubscribed_whenUserSubscribed_returnsTrue() {
		InterestUserList list = InterestUserList.setUpNewInterestUserList(interestId);
		list.addUser(userId);

		given(userListRepository.findByInterest(interestId)).willReturn(Optional.of(list));

		boolean result = interestService.isSubscribed(interestId, userId);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("isSubscribed: 구독하지 않은 경우 false 반환")
	void isSubscribed_whenUserNotSubscribed_returnsFalse() {
		InterestUserList list = InterestUserList.setUpNewInterestUserList(interestId);
		// userId를 추가하지 않음

		given(userListRepository.findByInterest(interestId)).willReturn(Optional.of(list));

		boolean result = interestService.isSubscribed(interestId, userId);

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("isSubscribed: 리스트가 존재하지 않는 경우 false 반환")
	void isSubscribed_whenListNotExist_returnsFalse() {
		given(userListRepository.findByInterest(interestId)).willReturn(Optional.empty());

		boolean result = interestService.isSubscribed(interestId, userId);

		assertThat(result).isFalse();
	}
}

