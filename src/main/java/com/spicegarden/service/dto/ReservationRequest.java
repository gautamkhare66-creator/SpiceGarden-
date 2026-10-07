package com.spicegarden.service.dto;

import java.time.LocalDateTime;

public record ReservationRequest(
        String customerName,
        String email,
        String phone,
        LocalDateTime dateTime,
        int partySize,
        String notes
) {
}
