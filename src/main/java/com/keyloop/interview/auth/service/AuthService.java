package com.keyloop.interview.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.keyloop.interview.auth.domain.AppUser;
import com.keyloop.interview.auth.domain.RefreshTokenEntity;
import com.keyloop.interview.auth.domain.UserRole;
import com.keyloop.interview.auth.dto.AuthRequests.RegisterCustomerRequest;
import com.keyloop.interview.auth.dto.AuthResponses.TokenPayload;
import com.keyloop.interview.auth.repository.RefreshTokenRepository;
import com.keyloop.interview.auth.repository.UserRepository;
import com.keyloop.interview.common.exception.DomainException;
import com.keyloop.interview.config.JwtProperties;
import com.keyloop.interview.customer.domain.Customer;
import com.keyloop.interview.customer.repository.CustomerRepository;
import com.keyloop.interview.security.JwtBlacklist;
import com.keyloop.interview.security.JwtTokenProvider;

import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository users;
	private final CustomerRepository customers;
	private final RefreshTokenRepository refreshTokens;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtProperties jwtProperties;
	private final JwtBlacklist jwtBlacklist;

	private static final SecureRandom RNG = new SecureRandom();

	@Transactional
	public TokenPayload registerCustomer(RegisterCustomerRequest req) {
		if (users.existsByEmail(req.email().trim().toLowerCase())) {
			throw new DomainException("EMAIL_ALREADY_USED", "Email is already registered.");
		}

		String[] nameParts = req.fullName().trim().split("\\s+", 2);
		String first = nameParts[0];
		String last = nameParts.length > 1 ? nameParts[1] : "";

		UUID userId = UUID.randomUUID();
		Instant now = Instant.now();
		AppUser u = AppUser.builder().id(userId).email(req.email().trim().toLowerCase())
				.passwordHash(passwordEncoder.encode(req.password()))
				.fullName(req.fullName().trim())
				.role(UserRole.CUSTOMER).dealershipId(null).active(true).emailVerified(false).createdAt(now)
				.updatedAt(now)
				.build();
		users.save(u);

		UUID customerId = UUID.randomUUID();
		Customer c = Customer.builder().id(customerId).user(u).firstName(first).lastName(last)
				.phone(req.phone())
				.email(req.email()).createdAt(now).updatedAt(now).build();
		customers.save(c);

		return issueTokens(u);
	}

	@Transactional(readOnly = true)
	public TokenPayload login(String email, String password) {
		AppUser user = users.findByEmail(email.trim().toLowerCase())
				.orElseThrow(() -> new DomainException("BAD_CREDENTIALS", "Invalid email or password."));

		if (!user.isActive() || !passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new DomainException("BAD_CREDENTIALS", "Invalid email or password.");
		}

		return issueTokens(user);
	}

	private TokenPayload issueTokens(AppUser user) {
		try {
			String access = jwtTokenProvider.mintAccessJwt(user.getId(), user.getEmail(), user.getRole(),
					user.getDealershipId()).serialize();
			long expSeconds = jwtProperties.getAccessTokenTtlMinutes() * 60L;

			String rawRefresh = randomToken();
			persistRefreshToken(user, rawRefresh);

			return TokenPayload.of(access, rawRefresh, expSeconds);
		} catch (com.nimbusds.jose.JOSEException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String randomToken() {
		byte[] b = new byte[32];
		RNG.nextBytes(b);
		return HexFormat.of().formatHex(b);
	}

	private void persistRefreshToken(AppUser user, String rawRefresh) {
		String hash = sha256(rawRefresh);
		Instant expiry = Instant.now().plus(jwtProperties.getRefreshTokenTtlDays(), ChronoUnit.DAYS);
		RefreshTokenEntity entity = RefreshTokenEntity.builder().id(UUID.randomUUID()).user(user)
				.tokenHash(hash).expiresAt(expiry).revoked(false).createdAt(Instant.now()).build();
		refreshTokens.save(entity);
	}

	private static String sha256(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	@Transactional
	public TokenPayload refresh(String rawRefresh) {
		String hash = sha256(rawRefresh);
		RefreshTokenEntity stored = refreshTokens.findByTokenHashAndRevokedFalse(hash)
				.orElseThrow(() -> new DomainException("INVALID_REFRESH", "Refresh token is invalid or revoked."));
		if (stored.getExpiresAt().isBefore(Instant.now())) {
			throw new DomainException("INVALID_REFRESH", "Refresh token expired.");
		}

		AppUser user = stored.getUser();
		stored.setRevoked(true);
		refreshTokens.save(stored);

		return issueTokens(user);
	}

	@Transactional
	public void logout(String authorizationHeaderBearerOptional, String rawRefreshOptional) {
		if (authorizationHeaderBearerOptional != null && authorizationHeaderBearerOptional.regionMatches(true, 0, "Bearer ", 0,
				7)) {
			String token = authorizationHeaderBearerOptional.substring("Bearer ".length()).trim();
			try {
				SignedJWT jwt = SignedJWT.parse(token);
				String jti = jwt.getJWTClaimsSet().getJWTID();
				if (jti != null && jwt.getJWTClaimsSet().getExpirationTime() != null)
					jwtBlacklist.blacklistJwtId(jti, jwt.getJWTClaimsSet().getExpirationTime().toInstant());
			} catch (java.text.ParseException ignored) {

			}
		}

		if (rawRefreshOptional != null && !rawRefreshOptional.isBlank()) {
			String h = sha256(rawRefreshOptional);
			refreshTokens.findByTokenHashAndRevokedFalse(h).ifPresent(t -> {
				t.setRevoked(true);
				refreshTokens.save(t);
			});
		}
	}
}
