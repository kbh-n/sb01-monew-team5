package com.example.part35teammonew.exeception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
	Instant timestamp,
	String code,
	String message,
	Map<String, Object> details,
	String exceptionType,
	int status
) {
}
