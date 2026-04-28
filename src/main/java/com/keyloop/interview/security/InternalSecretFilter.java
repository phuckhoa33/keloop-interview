package com.keyloop.interview.security;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyloop.interview.common.api.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class InternalSecretFilter extends OncePerRequestFilter {

	private static final ObjectMapper JSON = new ObjectMapper();

	private final com.keyloop.interview.config.AppProperties appProperties;

	public InternalSecretFilter(com.keyloop.interview.config.AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getServletPath().startsWith("/internal/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String secret = request.getHeader("X-Internal-Secret");
		String expected = appProperties.getInternal().getSecret();
		if (expected.isEmpty() || !expected.equals(secret)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			ErrorResponse er = ErrorResponse.builder()
					.timestamp(java.time.Instant.now())
					.status(403)
					.code("INTERNAL_AUTH_FAILED")
					.error("Forbidden")
					.message("Missing or invalid X-Internal-Secret header")
					.path(request.getRequestURI())
					.traceId("")
					.build();
			response.getWriter().write(JSON.writeValueAsString(er));
			return;
		}

		chain.doFilter(request, response);
	}
}
