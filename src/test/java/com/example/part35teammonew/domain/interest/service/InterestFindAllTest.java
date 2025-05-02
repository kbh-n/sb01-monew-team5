package com.example.part35teammonew.domain.interest.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.part35teammonew.domain.interest.entity.Interest;
import com.example.part35teammonew.domain.interest.repository.InterestRepository;

@ExtendWith(MockitoExtension.class)
public class InterestFindAllTest {
	@Mock
	private InterestRepository interestRepository;

	@InjectMocks
	private InterestServiceImpl interestService;

	@Test
	void getInterestsList_success() {
		// given
		UUID interestId = UUID.randomUUID();
		Interest interest = new Interest();
		interest.setId(interestId);
		interest.setName("뉴스");
		interest.setKeywords("정치,경제");

		given(interestRepository.findAll()).willReturn(List.of(interest));

		// when
		List<Pair<String, UUID>> result = interestService.getInterestList();

		// then
		List<String> values = result.stream().map(Pair::getLeft).collect(Collectors.toList());
		List<UUID> ids = result.stream().map(Pair::getRight).collect(Collectors.toList());

		assertThat(values).containsExactlyInAnyOrder("뉴스", "정치", "경제");
		assertThat(ids).allMatch(id -> id.equals(interestId));
	}

	@Test
	void getInterestsList_failure() {
		// given
		UUID interestId = UUID.randomUUID();
		Interest interest = new Interest();
		interest.setId(interestId);
		interest.setName("뉴스");
		interest.setKeywords(null); // 키워드 비워줌

		given(interestRepository.findAll()).willReturn(List.of(interest));

		// when & then
		assertThatThrownBy(() -> interestService.getInterestList())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("키워드는 비어 있을 수 없습니다");
	}

}
