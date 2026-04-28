package com.keyloop.interview.auth.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.keyloop.interview.auth.dto.AuthRequests.LoginRequest;
import com.keyloop.interview.auth.dto.AuthRequests.RefreshRequest;
import com.keyloop.interview.auth.dto.AuthRequests.RegisterCustomerRequest;
import com.keyloop.interview.auth.dto.AuthResponses.TokenPayload;
import com.keyloop.interview.auth.service.AuthService;
import com.keyloop.interview.security.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
public class AuthRestController {

	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public TokenPayload register(@Valid @RequestBody RegisterCustomerRequest request) {
		return authService.registerCustomer(request);
	}

	@PostMapping("/login")
	public TokenPayload login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request.email(), request.password());
	}

	@PostMapping("/refresh")
	public TokenPayload refresh(@Valid @RequestBody RefreshRequest request) {
		return authService.refresh(request.refreshToken());
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpServletRequest servletRequest,
			org.springframework.security.core.Authentication authenticationIgnored) {

		authService.logout(servletRequest.getHeader("Authorization"), servletRequest.getHeader("Refresh-Token"));
	}

	@GetMapping(path = ".well-known/jwks.json", produces = "application/json")
	public Map<String, Object> jwks() {
		return jwtTokenProvider.jwkSetDocument();
	}
}
