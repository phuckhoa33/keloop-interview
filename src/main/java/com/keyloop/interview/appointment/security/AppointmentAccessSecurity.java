package com.keyloop.interview.appointment.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.keyloop.interview.appointment.domain.Appointment;
import com.keyloop.interview.appointment.repository.AppointmentRepository;
import com.keyloop.interview.auth.domain.AppUser;
import com.keyloop.interview.auth.repository.UserRepository;
import com.keyloop.interview.customer.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Component("appointmentSecurity")
@RequiredArgsConstructor
public class AppointmentAccessSecurity {

	private final AppointmentRepository appointments;
	private final CustomerRepository customers;
	private final UserRepository users;

	public boolean canAccessAppointment(UUID appointmentId, Authentication auth) {
		UUID userId = UUID.fromString(auth.getName());
		Appointment appt = appointments.findById(appointmentId).orElse(null);
		if (appt == null)
			return false;

		AppUser user = users.findById(userId).orElse(null);
		if (user == null)
			return false;

		return switch (user.getRole()) {
			case ADMIN -> true;
			case ADVISOR -> user.getDealershipId() != null && user.getDealershipId().equals(appt.getDealershipId());
			case CUSTOMER -> customers.findByUser_Id(userId).map(c -> c.getId().equals(appt.getCustomerId())).orElse(false);
		};
	}
}
