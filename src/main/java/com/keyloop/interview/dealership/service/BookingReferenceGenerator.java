package com.keyloop.interview.dealership.service;

import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class BookingReferenceGenerator {

	private final EntityManager em;

	BookingReferenceGenerator(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public String nextBookingReference(Instant at) {
		long seq = ((Number) em.createNativeQuery("SELECT nextval('booking_ref_seq')").getSingleResult()).longValue();
		int year = at.atZone(ZoneOffset.UTC).getYear();
		return String.format("APT-%d-%06d", year, seq % 1_000_000L);
	}
}
