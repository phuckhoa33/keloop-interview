package com.keyloop.interview.infrastructure.redis;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.keyloop.interview.common.exception.ConflictException;

@Service
public class RedisLockService {

	private static final String PREFIX = "availability:lock:";

	private final StringRedisTemplate redisOptional;

	public RedisLockService(@Autowired(required = false) StringRedisTemplate redis) {
		this.redisOptional = redis;
	}

	public LockHandle acquire(String businessKey, Duration ttl) {
		StringRedisTemplate redis = redisOptional;
		if (redis == null) {
			// Fallback when Redis unavailable (e.g. JVM tests) — no cluster-wide mutual exclusion.
			return () -> {};
		}

		String token = UUID.randomUUID().toString();
		if (Boolean.FALSE.equals(redis.opsForValue().setIfAbsent(PREFIX + businessKey, token, ttl))) {
			throw new ConflictException("LOCK_NOT_ACQUIRED",
					"The selected time slot is being processed — try again shortly.");
		}

		return () -> {
			String stored = redis.opsForValue().get(PREFIX + businessKey);
			if (token.equals(stored))
				redis.delete(PREFIX + businessKey);
		};
	}

	@FunctionalInterface
	public interface LockHandle extends AutoCloseable {

		default void unlock() {
			close();
		}

		void close(); // unlocking / closing the lock reservation
	}
}
