package com.keyloop.interview.appointment.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

	@Id
	private UUID id;

	@Column(name = "booking_reference", nullable = false, unique = true)
	private String bookingReference;

	@Column(name = "customer_id", nullable = false)
	private UUID customerId;

	@Column(name = "vehicle_id", nullable = false)
	private UUID vehicleId;

	@Column(name = "dealership_id", nullable = false)
	private UUID dealershipId;

	@Column(name = "service_type_id", nullable = false)
	private UUID serviceTypeId;

	@Column(name = "technician_id", nullable = false)
	private UUID technicianId;

	@Column(name = "service_bay_id", nullable = false)
	private UUID serviceBayId;

	@Column(name = "requested_time", nullable = false)
	private Instant requestedTime;

	@Column(name = "start_time", nullable = false)
	private Instant startTime;

	@Column(name = "end_time", nullable = false)
	private Instant endTime;

	@Enumerated(EnumType.STRING)
	private AppointmentStatus status;

	private String notes;

	@Column(name = "cancellation_reason")
	private String cancellationReason;

	@Column(name = "total_price")
	private BigDecimal totalPrice;

	private Instant createdAt;
	private Instant updatedAt;

	@Column(name = "created_by", nullable = false)
	private UUID createdBy;

	@Builder.Default
	@Version
	private long version = 0L;
}
