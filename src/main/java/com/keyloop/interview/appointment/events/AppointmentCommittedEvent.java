package com.keyloop.interview.appointment.events;

import java.time.Instant;
import java.util.UUID;

import com.keyloop.interview.appointment.domain.Appointment;

public record AppointmentCommittedEvent(
		UUID appointmentId,
		String bookingReference,
		UUID dealershipId,
		UUID serviceBayId,
		UUID technicianId,
		String status,
		Instant startTime,
		Instant endTime,
		UUID customerUserIdForNotification) {

	public static AppointmentCommittedEvent from(Appointment a, UUID customerUserForNotification) {
		return new AppointmentCommittedEvent(a.getId(), a.getBookingReference(), a.getDealershipId(),
				a.getServiceBayId(), a.getTechnicianId(), a.getStatus().name(),
				a.getStartTime(), a.getEndTime(),
				customerUserForNotification);
	}
}
