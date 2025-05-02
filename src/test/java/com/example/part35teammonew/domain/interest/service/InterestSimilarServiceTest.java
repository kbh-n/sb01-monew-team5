package com.example.part35teammonew.domain.interest.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import com.example.part35teammonew.domain.interest.repository.InterestRepository;

@ExtendWith(MockitoExtension.class)
public class InterestSimilarServiceTest {

	@Mock
	private InterestRepository interestRepository;

	@InjectMocks
	private InterestServiceImpl interestService;

	@Test
	@DisplayName("저장된 관심사 x -> false 반환")
	void isNameTooSimilar_emptyRepository_returnsFalse() {

		//given
		given(interestRepository.findAllNames()).willReturn(List.of());

		//when
		boolean result = interestService.isNameTooSimilar("여행");

		//then
		assertThat(result).isFalse();

	}

	@Test
	@DisplayName("저장된 관심사와 80프로 이상 일치시 -> true 반환")
	void isNameTooSimilar_similarExists_returnsTrue() {

		//given
		given(interestRepository.findAllNames()).willReturn(List.of("제주도여행"));

		//when
		boolean result = interestService.isNameTooSimilar("제주도여행1");

		//then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("저장된 관심사와 유사도 통과 -> false 반환")
	void isNameTooSimilar_notSimilarEnough_returnsFalse() {
		//given
		given(interestRepository.findAllNames()).willReturn(List.of("제주도 여행"));

		//when
		boolean result = interestService.isNameTooSimilar("서울관광");

		//then
		assertThat(result).isFalse();
	}

}
