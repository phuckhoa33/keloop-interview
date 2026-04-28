package com.keyloop.interview.dealership.domain;

import java.math.BigDecimal;
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

@Entity
@Table(name = "service_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceType {

	@Id
	private UUID id;

	private String name;

	private String description;

	@Column(name = "duration_minutes", nullable = false)
	private int durationMinutes;

	@Column(name = "required_skill")
	private String requiredSkill;

	@Column(name = "base_price")
	private BigDecimal basePrice;

	@Column(name = "is_active")
	private boolean active;
}
