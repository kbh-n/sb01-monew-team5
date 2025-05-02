package com.example.part35teammonew.exeception;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
		ErrorResponse errorResponse = new ErrorResponse(
			Instant.now(),
			"NOT_FOUND",
			"잘못된 요청입니다.",
			Map.of("message", e.getMessage()),
			e.getClass().getSimpleName(),
			HttpStatus.NOT_FOUND.value()
		);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NoSuchElementException.class)
	protected ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
		ErrorResponse errorResponse = new ErrorResponse(
			Instant.now(),
			"NOT_FOUND_RESOURCE",
			"잘못된 요청입니다. 리소스를 찾을 수 없습니다.",
			Map.of("message", e.getMessage()),
			e.getClass().getSimpleName(),
			HttpStatus.NOT_FOUND.value()
		);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(RuntimeException.class)
	protected ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
		ErrorResponse errorResponse = new ErrorResponse(
			Instant.now(),
			"INTERNAL_SERVER_ERROR",
			"잘못된 요청입니다. 서버 내부 오류입니다.",
			Map.of("message", e.getMessage()),
			e.getClass().getSimpleName(),
			HttpStatus.INTERNAL_SERVER_ERROR.value()
		);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
