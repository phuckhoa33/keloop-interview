package com.keyloop.interview.appointment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public final class AppointmentDtos {

	private AppointmentDtos() {
	}

	public record BookingRequest(
			@NotNull UUID vehicleId,
			@NotNull UUID dealershipId,
			@NotNull UUID serviceTypeId,
			@NotNull Instant requestedStartTime,
			String notes) {
	}

	public record AppointmentResponse(UUID id, String bookingReference, String status, VehicleSummary vehicle,
			DealershipSummary dealership, ServiceTypeSummary serviceType, PersonSummary technician,
			BaySummary serviceBay,
			Instant startTime, Instant endTime, BigDecimal totalPrice, Instant createdAt) {

	}

	public record VehicleSummary(UUID id, String make, String model, int year) {

	}

	public record DealershipSummary(UUID id, String name) {

	}

	public record ServiceTypeSummary(UUID id, String name, int durationMinutes) {

	}

	public record PersonSummary(UUID id, String firstName, String lastName) {

	}

	public record BaySummary(UUID id, String bayNumber, String bayType) {

	}

	public record CancelReason(String reason) {
	}

	public record StatusPatch(String status) {
	}
}
