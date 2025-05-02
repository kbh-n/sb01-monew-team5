package com.example.part35teammonew.domain.interest.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PageResponse<T>(
	List<T> content,
	String nextCursor,
	LocalDateTime nextAfter,
	int size,
	Long totalElements,
	boolean hasNext
) {
}