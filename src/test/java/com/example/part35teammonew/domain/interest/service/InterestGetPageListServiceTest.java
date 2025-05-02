package com.example.part35teammonew.domain.interest.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.part35teammonew.domain.interest.dto.request.InterestPageRequest;
import com.example.part35teammonew.domain.interest.dto.response.PageResponse;
import com.example.part35teammonew.domain.interest.dto.response.InterestDto;
import com.example.part35teammonew.domain.interest.entity.Interest;
import com.example.part35teammonew.domain.interest.repository.InterestRepository;
import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import com.example.part35teammonew.domain.interestUserList.repository.InterestUserListRepository;

@ExtendWith(MockitoExtension.class)
public class InterestGetPageListServiceTest {
	@Mock
	private InterestRepository interestRepository;

	@Mock
	private InterestUserListRepository userListRepository;

	@InjectMocks
	private InterestServiceImpl interestService;

	@Test
	@DisplayName("검색어 주어지면 searchByNameOrKeyword() 호출 후 DTO 매핑")
	void listInterests_search_success() {
		//given
		String search = "제주도";
		UUID userId = UUID.randomUUID();

		Interest interest1 = new Interest();
		interest1.setId(UUID.randomUUID());
		interest1.setName("여행");
		interest1.setKeywords("서울,제주도");

		Interest interest2 = new Interest();
		interest2.setId(UUID.randomUUID());
		interest2.setName("지역");
		interest2.setKeywords("제주도,부산");

		List<Interest> interestList = List.of(interest1, interest2);
		PageImpl<Interest> page = new PageImpl<>(interestList, PageRequest.of(0, 10, Sort.by("name").ascending()),
			interestList.size());
		given(interestRepository.searchByNameOrKeyword(eq(search), any(Pageable.class))).willReturn(page);

		//Mongo db 첫번째에 user id 포함
		InterestUserList userInterestList = InterestUserList.setUpNewInterestUserList(interest1.getId());
		userInterestList.addUser(userId);
		given(userListRepository.findByInterest(interest1.getId())).willReturn(Optional.of(userInterestList));
		given(userListRepository.findByInterest(interest2.getId())).willReturn(Optional.empty());

		// 요청 객체
		InterestPageRequest request = new InterestPageRequest(
			search,
			"name", // orderBy
			"asc",  // direction
			null,   // cursor
			null,   // after
			10,
			userId
		);

		//when
		PageResponse<InterestDto> result = interestService.listInterests(request);

		//then

		List<InterestDto> dtoList = result.content();
		assertThat(dtoList.get(0).getId()).isEqualTo(interest1.getId());
		assertThat(dtoList.get(0).getName()).isEqualTo("여행");
		assertThat(dtoList.get(0).getKeywords()).containsExactly("서울", "제주도");
		assertThat(dtoList.get(0).getSubscriberCount()).isEqualTo(1);
		assertThat(dtoList.get(0).isSubscribedByMe()).isTrue();

		assertThat(dtoList.get(1).getId()).isEqualTo(interest2.getId());
		assertThat(dtoList.get(1).getName()).isEqualTo("지역");
		assertThat(dtoList.get(1).getKeywords()).containsExactly("제주도", "부산");
		assertThat(dtoList.get(1).getSubscriberCount()).isEqualTo(0);
		assertThat(dtoList.get(1).isSubscribedByMe()).isFalse();
	}

	@Test
	@DisplayName("검색어가 없고 커서랑 정렬기준만 주어 졌을때 -> 이름 오름차순")
	void listInterests_cursorName_asc() {
		//given
		UUID userId = UUID.randomUUID();
		int size = 2;
		String lastName = "지역";

		Interest interest1 = new Interest();
		interest1.setId(UUID.randomUUID());
		interest1.setName("스포츠");
		interest1.setKeywords("농구,배구");

		Interest interest2 = new Interest();
		interest2.setId(UUID.randomUUID());
		interest2.setName("게임");
		interest2.setKeywords("롤,피파");

		List<Interest> pageList = List.of(interest1, interest2);
		Pageable pageable = PageRequest.of(0, size, Sort.by("name").ascending());
		given(interestRepository.findByNameAfter(eq(lastName), isNull(), any(Pageable.class)))
			.willReturn(pageList);
		// mongodb
		InterestUserList userInterestList = InterestUserList.setUpNewInterestUserList(interest1.getId());
		userInterestList.addUser(userId);
		given(userListRepository.findByInterest(interest1.getId())).willReturn(Optional.of(userInterestList));
		given(userListRepository.findByInterest(interest2.getId())).willReturn(Optional.empty());

		InterestPageRequest request = new InterestPageRequest(
			null,        // keyword
			"name",      // orderBy
			"asc",       // direction
			lastName,    // cursor (이름 기준 커서)
			null,        // after (createdAt)
			size,
			userId
		);

		//when
		PageResponse<InterestDto> result = interestService.listInterests(request);

		//then
		assertThat(result.content()).hasSize(2);

		InterestDto dto1 = result.content().get(0);
		assertThat(dto1.getName()).isEqualTo("스포츠");
		assertThat(dto1.getKeywords()).containsExactly("농구", "배구");
		assertThat(dto1.getSubscriberCount()).isEqualTo(1);
		assertThat(dto1.isSubscribedByMe()).isTrue();

		InterestDto dto2 = result.content().get(1);
		assertThat(dto2.getName()).isEqualTo("게임");
		assertThat(dto2.getSubscriberCount()).isEqualTo(0);

	}

	@Test
	@DisplayName("검색어가 없고 커서랑 정렬기준만 주어 졌을때 -> 구독자 수 오름차순")
	void listInterests_cursorCount_asc() {
		UUID userId = UUID.randomUUID();
		int size = 3;
		String lastCount = "10";

		Interest interest1 = new Interest();
		interest1.setId(UUID.randomUUID());

		Interest interest2 = new Interest();
		interest2.setId(UUID.randomUUID());

		Interest interest3 = new Interest();
		interest3.setId(UUID.randomUUID());

		List<Interest> pageList = List.of(interest1, interest2, interest3);
		Pageable pageable = PageRequest.of(0, size, Sort.by("subscriberCount").ascending());
		given(interestRepository.findBySubscriberCountAfter(eq(10L), isNull(), any(Pageable.class)))
			.willReturn(pageList);

		InterestUserList list1 = InterestUserList.setUpNewInterestUserList(interest1.getId());
		InterestUserList list2 = InterestUserList.setUpNewInterestUserList(interest2.getId());
		InterestUserList list3 = InterestUserList.setUpNewInterestUserList(interest3.getId());

		for (int i = 0; i < 5; i++) {
			list1.addUser(UUID.randomUUID());
		}
		for (int i = 0; i < 7; i++) {
			list2.addUser(UUID.randomUUID());
		}
		for (int i = 0; i < 9; i++) {
			list3.addUser(UUID.randomUUID());
		}

		given(userListRepository.findByInterest(interest1.getId())).willReturn(Optional.of(list1));
		given(userListRepository.findByInterest(interest2.getId())).willReturn(Optional.of(list2));
		given(userListRepository.findByInterest(interest3.getId())).willReturn(Optional.of(list3));

		InterestPageRequest request = new InterestPageRequest(
			null,
			"subscriberCount",
			"asc",
			lastCount,
			null,
			size,
			userId
		);

		//when
		PageResponse<InterestDto> result = interestService.listInterests(request);

		//then
		assertThat(result.content()).hasSize(3);
		assertThat(result.content()).extracting(InterestDto::getSubscriberCount).containsExactly(5L, 7L, 9L);

	}

}
