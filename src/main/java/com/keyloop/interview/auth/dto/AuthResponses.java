package com.keyloop.interview.auth.dto;

import java.time.Instant;
import java.util.UUID;

public final class AuthResponses {

	private AuthResponses() {
	}

	public record TokenPayload(String accessToken, String refreshToken, String tokenType, long expiresIn,
			String scope) {

		public static TokenPayload of(String accessToken, String refreshToken, long expiresSeconds) {
			return new TokenPayload(accessToken, refreshToken, "Bearer", expiresSeconds, "openid profile");
		}
	}

	public record UserSummary(UUID userId, String email, String fullName, String role, UUID dealershipId) {
	}
}
