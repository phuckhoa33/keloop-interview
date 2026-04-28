package com.keyloop.interview.appointment.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.keyloop.interview.appointment.domain.AppointmentAuditLog;

public interface AppointmentAuditLogRepository extends JpaRepository<AppointmentAuditLog, UUID> {

	List<AppointmentAuditLog> findByAppointmentIdOrderByPerformedAtAsc(UUID appointmentId);
}
