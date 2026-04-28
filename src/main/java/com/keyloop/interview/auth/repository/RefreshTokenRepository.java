package com.keyloop.interview.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keyloop.interview.auth.domain.RefreshTokenEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

	Optional<RefreshTokenEntity> findByTokenHashAndRevokedFalse(String tokenHash);

	void deleteByUserIdAndExpiresAtBefore(UUID userId, Instant now);
}
