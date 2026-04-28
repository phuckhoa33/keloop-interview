package com.keyloop.interview.appointment.listener;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.keyloop.interview.appointment.events.AppointmentCommittedEvent;
import com.keyloop.interview.dealership.domain.AppointmentSlot;
import com.keyloop.interview.dealership.repository.AppointmentSlotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentCommittedListener {

	private final AppointmentSlotRepository slots;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onCommitted(AppointmentCommittedEvent e) {

		UUID bayRowId = UUID.randomUUID();
		UUID techRowId = UUID.randomUUID();

		slots.save(AppointmentSlot.builder().id(bayRowId).resourceType("BAY").resourceId(e.serviceBayId())
				.startTime(e.startTime()).endTime(e.endTime()).status(e.status()).build());
		slots.save(AppointmentSlot.builder().id(techRowId).resourceType("TECHNICIAN").resourceId(e.technicianId())
				.startTime(e.startTime()).endTime(e.endTime()).status(e.status()).build());

		log.info("[notification-stub] appointment {} confirmed — would notify customer user {}", e.bookingReference(),
				e.customerUserIdForNotification());
	}
}
