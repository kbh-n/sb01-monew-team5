package com.example.part35teammonew.domain.interest.service;

import com.example.part35teammonew.exeception.InterestNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.part35teammonew.domain.interest.dto.response.InterestDto;
import com.example.part35teammonew.domain.interest.entity.Interest;
import com.example.part35teammonew.domain.interest.repository.InterestRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class InterestUpdateServiceTest {
	@Mock
	private InterestRepository interestRepository;

	@InjectMocks
	private InterestServiceImpl interestService;

	@Test
	@DisplayName("없는 아이디로 호출시 예외")
	void updateKeywords_notFound() {
		UUID id = UUID.randomUUID();

		//given
		given(interestRepository.findById(id)).willReturn(Optional.empty());

		assertThatThrownBy(() -> interestService.updateKeywords(id, List.of("A", "B")))
			.isInstanceOf(InterestNotFoundException.class)
			.hasMessageContaining("관심사를 찾을 수 없습니다: id 오류");

		verify(interestRepository, never()).save(any());
	}

	@Test
	@DisplayName("updateKeywords() -> 기존 관심사가 있을 경우 키워드만 수정 성공")
	void updateKeywords_success() {
		UUID id = UUID.randomUUID();

		//given
		// 기존 엔티티
		Interest existing = new Interest();
		existing.setId(id);
		existing.setName("여행");
		existing.setKeywords("제주도,서울");
		existing.setSubscriberCount(5);
		existing.setSubscribedMe(true);
		given(interestRepository.findById(id)).willReturn(Optional.of(existing));

		//수정된 엔티티
		Interest saved = new Interest();
		saved.setId(id);
		saved.setName("여행");
		saved.setKeywords("부산,대구");
		saved.setSubscriberCount(5);
		saved.setSubscribedMe(true);
		given(interestRepository.save(any(Interest.class)))
			.willReturn(saved);

		//when
		List<String> newKeywords = List.of("부산", "대구");
		InterestDto dto = interestService.updateKeywords(id, newKeywords);

		//then
		assertThat(dto.getId()).isEqualTo(id);
		assertThat(dto.getName()).isEqualTo("여행");
		assertThat(dto.getKeywords()).containsExactly("부산", "대구");
		assertThat(dto.getSubscriberCount()).isEqualTo(5);
		assertThat(dto.isSubscribedByMe()).isTrue();

		// keywords 가 제대로 수정 됐는지 확인
		ArgumentCaptor<Interest> captor = ArgumentCaptor.forClass(Interest.class);
		verify(interestRepository).save(captor.capture());
		assertThat(captor.getValue().getKeywords()).isEqualTo("부산,대구");

	}

}
