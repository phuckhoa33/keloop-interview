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

@Entity
@Table(name = "dealerships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dealership {

	@Id
	private UUID id;

	private String name;

	private String address;

	private String phone;

	private String email;

	private String timezone;

	@Column(name = "is_active")
	private boolean active;

	private Instant createdAt;
}
