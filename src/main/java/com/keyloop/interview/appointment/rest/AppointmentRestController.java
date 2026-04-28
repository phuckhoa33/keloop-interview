package com.keyloop.interview.appointment.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keyloop.interview.appointment.dto.AppointmentDtos.AppointmentResponse;
import com.keyloop.interview.appointment.dto.AppointmentDtos.BookingRequest;
import com.keyloop.interview.appointment.dto.AppointmentDtos.CancelReason;
import com.keyloop.interview.appointment.service.AppointmentOrchestrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentRestController {

	private final AppointmentOrchestrationService orchestrationService;

	@PostMapping
	@PreAuthorize("hasAnyRole('CUSTOMER','ADVISOR','ADMIN')")
	public AppointmentResponse book(@Valid @RequestBody BookingRequest req, Authentication authentication) {
		var userId = UUID.fromString(authentication.getName());
		return orchestrationService.book(req, userId);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@appointmentSecurity.canAccessAppointment(#id)")
	public AppointmentResponse one(@PathVariable UUID id) {
		return orchestrationService.get(id);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('CUSTOMER','ADVISOR','ADMIN')")
	public List<AppointmentResponse> list(Authentication authentication) {
		var userId = UUID.fromString(authentication.getName());
		return orchestrationService.list(userId);
	}

	@PatchMapping("/{id}/cancel")
	@PreAuthorize("@appointmentSecurity.canAccessAppointment(#id)")
	public void cancel(@PathVariable UUID id, Authentication authentication,
			@RequestBody(required = false) CancelReason cancelReasonInput) {
		var userId = UUID.fromString(authentication.getName());
		String reasonMaybe = cancelReasonInput == null ? null : cancelReasonInput.reason();
		orchestrationService.cancel(id, userId, reasonMaybe);
	}
}
