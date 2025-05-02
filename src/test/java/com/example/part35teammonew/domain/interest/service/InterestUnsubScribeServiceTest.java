package com.example.part35teammonew.domain.interest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import com.example.part35teammonew.domain.interest.entity.Interest;
import com.example.part35teammonew.domain.interest.repository.InterestRepository;
import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import com.example.part35teammonew.domain.interestUserList.repository.InterestUserListRepository;
import com.example.part35teammonew.exeception.InterestNotFoundException;

@ExtendWith(MockitoExtension.class)
public class InterestUnsubScribeServiceTest {

	@Mock
	private InterestRepository interestRepository;

	@Mock
	private InterestUserListRepository userListRepository;

	@InjectMocks
	private InterestServiceImpl interestService;

	private UUID interestId;
	private UUID userId;
	private Interest interest;

	@BeforeEach
	void setUp() {
		interestId = UUID.randomUUID();
		userId = UUID.randomUUID();
		interest = new Interest();
		interest.setId(interestId);
		interest.setName("name");
		interest.setKeywords("key,word");
		interest.setSubscriberCount(1L);
	}

	@Test
	@DisplayName("존재하지 않는 ID 로 호출시 예외")
	void unsubscribe_notFound() {
		//given
		given(interestRepository.findById(interestId)).willReturn(Optional.empty());

		//when / then
		assertThatThrownBy(()-> interestService.unsubscribe(interestId, userId))
			.isInstanceOf(InterestNotFoundException.class)
			.hasMessageContaining("관심사를 찾을 수 없습니다: id 오류");

		verify(userListRepository,never()).findByInterest(any());
		verify(userListRepository,never()).save(any());
		verify(interestRepository,never()).save(any());
	}

	@Test
	@DisplayName("이미 구독중인 경우 Mongodb에서 제거 하고 subscriberCount 감소 및 저장")
	void unsubscribe_success(){
		//given
		given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
		InterestUserList userList =InterestUserList.setUpNewInterestUserList(interestId);
		userList.addUser(userId);
		userList.addUser(UUID.randomUUID());
		given(userListRepository.findByInterest(interestId)).willReturn(Optional.of(userList));

		//when
		interestService.unsubscribe(interestId, userId);

		//then
		assertThat(userList.findUser(userId)).isFalse();
		assertThat(userList.getUserCount()).isEqualTo(1L);
		verify(userListRepository).save(userList);
		verify(interestRepository).save(argThat(i -> i.getSubscriberCount() == 0L));
	}

	@Test
	@DisplayName("구독중이 아닌데 구독취소를 한 경우")
	void unsubscribe_fail(){
		//given
		given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
		InterestUserList userList =InterestUserList.setUpNewInterestUserList(interestId);
		given(userListRepository.findByInterest(interestId)).willReturn(Optional.of(userList));

		//when then
		assertThatThrownBy(() -> interestService.unsubscribe(interestId, userId))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("사용자가 구독중이 아닙니다.");

		verify(userListRepository, never()).save(any());
		verify(interestRepository, never()).save(any());
	}
}
