package com.keyloop.interview.security;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class SecurityHeadersFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		response.addHeader("X-Content-Type-Options", "nosniff");
		response.addHeader("X-Frame-Options", "DENY");
		response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
		response.addHeader("X-XSS-Protection", "1; mode=block");
		response.addHeader("Content-Security-Policy", "default-src 'self'");

		chain.doFilter(request, response);
	}
}
