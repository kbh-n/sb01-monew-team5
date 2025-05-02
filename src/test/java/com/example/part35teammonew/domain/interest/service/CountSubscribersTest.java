package com.example.part35teammonew.domain.interest.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import com.example.part35teammonew.domain.interestUserList.repository.InterestUserListRepository;

@ExtendWith(MockitoExtension.class)
public class CountSubscribersTest {

	@Mock
	private InterestUserListRepository userListRepository;


	@InjectMocks
	private InterestServiceImpl interestService;

	private UUID interestId;

	@BeforeEach
	void setUp() {
		interestId = UUID.randomUUID();
	}

	@Test
	@DisplayName("countSubscribers: 구독자 리스트가 존재할 경우 정확한 카운트를 반환한다")
	void countSubscribers_exist_returnsCount() {

		System.out.println("userListRepository = " + userListRepository);
		System.out.println("interestService.userListRepository = " +
			Arrays.stream(InterestServiceImpl.class.getDeclaredFields())
				.filter(f -> f.getName().equals("userListRepository"))
				.map(f -> {
					f.setAccessible(true);
					try {
						return f.get(interestService);
					} catch (IllegalAccessException e) {
						return "error";
					}
				}).findFirst().orElse("not found"));

		InterestUserList list = InterestUserList.setUpNewInterestUserList(interestId);
		list.addUser(UUID.randomUUID());
		list.addUser(UUID.randomUUID());
		list.addUser(UUID.randomUUID());

		given(userListRepository.findByInterest(interestId)).willReturn(Optional.of(list));

		long count = interestService.countSubscribers(interestId);


		assertThat(count).isEqualTo(3);
	}

	@Test
	@DisplayName("countSubscribers: 리스트가 없을 경우 0을 반환한다")
	void countSubscribers_notExist_returnsZero() {
		when(userListRepository.findByInterest(interestId)).thenReturn(Optional.empty());

		long count = interestService.countSubscribers(interestId);

		assertThat(count).isZero();
	}

}
