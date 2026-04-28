package com.keyloop.interview.appointment.domain;

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
@Table(name = "appointment_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentAuditLog {

	@Id
	private UUID id;

	@Column(name = "appointment_id", nullable = false)
	private UUID appointmentId;

	private String action;

	@Column(name = "old_status")
	private String oldStatus;

	@Column(name = "new_status")
	private String newStatus;

	@Column(name = "performed_by", nullable = false)
	private UUID performedBy;

	@Column(name = "performed_at", nullable = false)
	private Instant performedAt;

	private String metadata;
}
