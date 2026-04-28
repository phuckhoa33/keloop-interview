package com.keyloop.interview.dealership.web.dto;

import java.time.Instant;
import java.util.UUID;

public record AvailableSlotSuggestion(
	Instant startTime,
	Instant endTime,
	UUID technicianIdHint,
	String technicianName,
	UUID serviceBayIdHint,
	String bayNumber) {

}
