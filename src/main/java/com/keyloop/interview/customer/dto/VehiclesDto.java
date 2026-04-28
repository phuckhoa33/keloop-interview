package com.keyloop.interview.customer.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class VehiclesDto {

	private VehiclesDto() {
	}

	public record VehicleUpsert(@NotBlank @Size(max = 17) String vin,
			@NotBlank String make,
			@NotBlank String model,
			@NotNull Integer year,
			String licensePlate,
			String color,
			Integer mileage) {

	}

	public record VehicleResponse(UUID id, String vin, String make, String model, int year,
			String licensePlate, String color, Integer mileage) {

	}

	public record OwnershipCheck(Boolean owned) {
	}
}
