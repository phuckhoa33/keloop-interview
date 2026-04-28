package com.keyloop.interview.common.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.keyloop.interview.common.api.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
		log.warn("Resource not found: {}", ex.getMessage());
		return ResponseEntity.status(404).body(build(404, "NOT_FOUND", "RESOURCE_NOT_FOUND", ex.getMessage(), req));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> conflict(ConflictException ex, HttpServletRequest req) {
		return ResponseEntity.status(409).body(build(409, "CONFLICT", ex.getCode(), ex.getMessage(), req));
	}

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ErrorResponse> domain(DomainException ex, HttpServletRequest req) {
		if ("BAD_CREDENTIALS".equals(ex.getCode())) {
			return ResponseEntity.status(401).body(build(401, "UNAUTHORIZED", ex.getCode(), ex.getMessage(), req));
		}
		if ("FORBIDDEN".equals(ex.getCode())) {
			return ResponseEntity.status(403).body(build(403, "FORBIDDEN", ex.getCode(), ex.getMessage(), req));
		}
		return ResponseEntity.status(422).body(build(422, "UNPROCESSABLE_ENTITY", ex.getCode(), ex.getMessage(), req));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ": " + e.getDefaultMessage())
				.collect(Collectors.joining(", "));
		return ResponseEntity.badRequest().body(build(400, "BAD_REQUEST", "VALIDATION_FAILED", msg, req));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> forbidden(AccessDeniedException ex, HttpServletRequest req) {
		return ResponseEntity.status(403).body(build(403, "FORBIDDEN", "FORBIDDEN", "Insufficient permissions", req));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest req) {
		log.error("Unhandled error", ex);
		return ResponseEntity.status(500)
				.body(build(500, "INTERNAL_ERROR", "INTERNAL_ERROR", "An unexpected error occurred", req));
	}

	private static ErrorResponse build(int status, String error, String code, String message, HttpServletRequest req) {
		return ErrorResponse.builder()
				.timestamp(Instant.now())
				.status(status)
				.error(error)
				.code(code)
				.message(message)
				.path(req.getRequestURI())
				.traceId(MDC.get("traceId"))
				.build();
	}
}
