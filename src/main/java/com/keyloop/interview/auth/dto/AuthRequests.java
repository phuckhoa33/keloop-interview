package com.keyloop.interview.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthRequests {

	private AuthRequests() {
	}

	public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
	}

	public record RegisterCustomerRequest(@NotBlank @Email String email, @NotBlank @Size(min = 8) String password,
			@NotBlank String fullName,
			String phone) {
	}

	public record RefreshRequest(@NotBlank String refreshToken) {
	}
}
