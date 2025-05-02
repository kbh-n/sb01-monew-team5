package com.example.part35teammonew.domain.interest.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.part35teammonew.domain.interest.entity.Interest;
import com.example.part35teammonew.domain.interest.repository.InterestRepository;
import com.example.part35teammonew.exeception.InterestNotFoundException;

@ExtendWith(MockitoExtension.class)
public class InterestDeleteServiceTest {
	@Mock
	private InterestRepository interestRepository;

	@InjectMocks
	private InterestServiceImpl interestService;

	@Test
	@DisplayName("없는 ID로 호출하면 예외 처리")
	void deleteInterest_notFound() {
		UUID id = UUID.randomUUID();
		//given
		given(interestRepository.findById(id)).willReturn(Optional.empty());

		//when then
		assertThatThrownBy(() -> interestService.deleteInterest(id))
			.isInstanceOf(InterestNotFoundException.class)
			.hasMessageContaining("관심사를 찾을 수 없습니다: id 오류");

		verify(interestRepository, never()).deleteById(any());
	}

	@Test
	@DisplayName("관심사 삭제 성공")
	void deleteInterest_success() {
		//given
		UUID id = UUID.randomUUID();
		Interest interest = new Interest();
		interest.setId(id);
		interest.setName("여행");
		interest.setKeywords("제주도,서울,부산");
		given(interestRepository.findById(id)).willReturn(Optional.of(interest));

		//when
		interestService.deleteInterest(id);

		//then
		verify(interestRepository).findById(id);
		verify(interestRepository).delete(interest);
	}
}
