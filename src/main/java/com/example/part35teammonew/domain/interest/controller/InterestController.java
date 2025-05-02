package com.example.part35teammonew.domain.interest.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.part35teammonew.domain.interest.dto.request.InterestCreateRequest;
import com.example.part35teammonew.domain.interest.dto.request.InterestPageRequest;
import com.example.part35teammonew.domain.interest.dto.response.PageResponse;
import com.example.part35teammonew.domain.interest.dto.response.InterestDto;
import com.example.part35teammonew.domain.interest.dto.request.InterestUpdateRequest;
import com.example.part35teammonew.domain.interest.service.InterestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
@Validated
public class InterestController {

	private final InterestService interestService;

	@PostMapping
	public ResponseEntity<InterestDto> create(@RequestBody @Valid InterestCreateRequest request) {
		InterestDto dto = interestService.createInterest(request);
		return new ResponseEntity<>(dto, HttpStatus.CREATED);
	}

	@PatchMapping("{interestId}")
	public ResponseEntity<InterestDto> updateKeywords(@PathVariable UUID interestId,
		@RequestBody @Valid InterestUpdateRequest request) {
		InterestDto dto = interestService.updateKeywords(interestId, request.getKeywords());
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@DeleteMapping("{interestId}")
	public ResponseEntity<InterestDto> delete(@PathVariable UUID interestId) {
		interestService.deleteInterest(interestId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("{interestId}/subscriptions")
	public ResponseEntity<Void> subscribe(@PathVariable UUID interestId,
		@RequestHeader("Monew-Request-User-ID") UUID userId) {
		interestService.subscribe(interestId, userId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping
	public ResponseEntity<PageResponse<InterestDto>> listInterests(
		@RequestParam(required = false) String keyword,
		@RequestParam String orderBy,
		@RequestParam String direction,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
		@RequestParam int limit,
		@RequestHeader("Monew-Request-User-ID") UUID userId
	) {
		InterestPageRequest request = new InterestPageRequest(
			keyword,
			orderBy,
			direction,
			cursor,
			after,
			limit,
			userId
		);
		PageResponse<InterestDto> result = interestService.listInterests(request);
		return ResponseEntity.ok(result);
	}

	@DeleteMapping("{interestId}/subscriptions")
	public ResponseEntity<Void> unsubscribe(@PathVariable UUID interestId,
		@RequestHeader("Monew-Request-User-ID") UUID userId) {
		interestService.unsubscribe(interestId, userId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
