package com.example.part35teammonew.domain.interest.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.part35teammonew.domain.interest.entity.Interest;
import com.example.part35teammonew.domain.interest.repository.InterestRepository;
import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import com.example.part35teammonew.domain.interestUserList.repository.InterestUserListRepository;
import com.example.part35teammonew.exeception.AlreadySubscribedException;
import com.example.part35teammonew.exeception.InterestNotFoundException;

@ExtendWith(MockitoExtension.class)
public class InterestSubscribeServiceTest {

	@Mock
	private InterestRepository interestRepository;

	@Mock
	private InterestUserListRepository userListRepository;

	@InjectMocks
	private InterestServiceImpl interestService;

	@Test
	@DisplayName("구독 : 존재하지 않는 관심사로 ID로 호출")
	void subscribe_notFound() {
		//given
		UUID interestId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		given(interestRepository.findById(interestId)).willReturn(Optional.empty());

		//when then
		assertThatThrownBy(() -> interestService.subscribe(interestId, userId))
			.isInstanceOf(InterestNotFoundException.class)
			.hasMessageContaining("관심사를 찾을 수 없습니다: id 오류");

		verify(userListRepository, never()).findByInterest(interestId);
		verify(interestRepository, never()).save(any());

	}

	@Test
	@DisplayName("구독 : 구독 성공 -> Mongo 저장, RDB 구독자 수 동기화")
	void subscribe_success() {
		//given
		UUID interestId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Interest interest = new Interest();
		interest.setId(interestId);
		given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
		given(userListRepository.findByInterest(interestId))
			.willReturn(Optional.empty());

		//when
		interestService.subscribe(interestId, userId);

		//then
		ArgumentCaptor<InterestUserList> mongoCaptor = ArgumentCaptor.forClass(InterestUserList.class);
		verify(userListRepository).save(mongoCaptor.capture());
		InterestUserList list = mongoCaptor.getValue();
		assertThat(list.getInterest()).isEqualTo(interestId);
		assertThat(list.findUser(userId)).isTrue();

		ArgumentCaptor<Interest> repoCaptor = ArgumentCaptor.forClass(Interest.class);
		verify(interestRepository).save(repoCaptor.capture());
		assertThat(repoCaptor.getValue().getSubscriberCount()).isEqualTo(1);

	}

	@Test
	@DisplayName("이미 구독한 사용자는 예외 처리")
	void subscribe_alreadySubscribed() {
		//given
		UUID interestId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Interest interest = new Interest();
		interest.setId(interestId);
		interest.setSubscriberCount(5);
		given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));

		//mongodb 에 이미 리스트가 있을때
		InterestUserList list = InterestUserList.setUpNewInterestUserList(interestId);
		list.addUser(userId);
		given(userListRepository.findByInterest(interestId)).willReturn(Optional.of(list));

		//when then
		assertThatThrownBy(() -> interestService.subscribe(interestId, userId))
			.isInstanceOf(AlreadySubscribedException.class)
			.hasMessageContaining("이미 구독중 입니다.");

		verify(userListRepository, never()).save(any());
		verify(interestRepository, never()).save(any());
	}
}
