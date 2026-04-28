package com.keyloop.interview.auth.domain;

public enum UserRole {
	CUSTOMER,
	ADVISOR,
	ADMIN;

	public static String toAuthority(UserRole role) {
		return "ROLE_" + role.name();
	}
}
