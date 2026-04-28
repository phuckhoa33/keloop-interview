package com.keyloop.interview.dealership.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Local projection maintained after appointment commits (parity with CQRS/read model). */
@Entity
@Table(name = "appointment_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentSlot {

	@Id
	private UUID id;

	@Column(name = "resource_type", nullable = false)
	private String resourceType;

	@Column(name = "resource_id", nullable = false)
	private UUID resourceId;

	@Column(name = "start_time", nullable = false)
	private Instant startTime;

	@Column(name = "end_time", nullable = false)
	private Instant endTime;

	@Column(nullable = false)
	private String status;
}
