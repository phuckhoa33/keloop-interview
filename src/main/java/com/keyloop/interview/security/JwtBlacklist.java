package com.keyloop.interview.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JwtBlacklist {

	private static final String KEY = "jwt:blacklist:";

	private final StringRedisTemplate redisOptional;

	JwtBlacklist(@Autowired(required = false) StringRedisTemplate redis) {
		this.redisOptional = redis;
	}

	public void blacklistJwtId(String jwtId, Instant expiresAt) {

		StringRedisTemplate redis = redisOptional;
		if (redis == null)
			return;
		if (jwtId == null)
			return;

		long secs = Duration.between(Instant.now(), expiresAt).getSeconds();

		if (secs > 0) {
			redis.opsForValue().set(KEY + jwtId, "1", secs, TimeUnit.SECONDS);
		}
	}

	public boolean isListed(String jwtId) {
		StringRedisTemplate redis = redisOptional;
		return redis != null && jwtId != null && Boolean.TRUE.equals(redis.hasKey(KEY + jwtId));
	}

}
