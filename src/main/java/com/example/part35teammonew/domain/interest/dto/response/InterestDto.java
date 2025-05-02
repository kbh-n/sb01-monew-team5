package com.example.part35teammonew.domain.interest.dto.response;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.example.part35teammonew.domain.interest.entity.Interest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestDto {
	private UUID id;

	private String name;

	private List<String> keywords;

	private long subscriberCount;

	private boolean subscribedByMe;

	public static InterestDto toDto(Interest interest) {
		List<String> keywordList = Arrays.stream(interest.getKeywords().split(","))
			.map(String::strip)
			.toList();
		return new InterestDto(
			interest.getId(),
			interest.getName(),
			keywordList,
			interest.getSubscriberCount(),
			interest.isSubscribedMe()
		);
	}
}
