package com.spicegarden.service;

import com.spicegarden.domain.Reservation;
import com.spicegarden.repository.ReservationRepository;
import com.spicegarden.service.dto.ReservationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationService {
    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
    }

    public Reservation createReservation(ReservationRequest request) {
        if (request.partySize() <= 0 || request.partySize() > 10) {
            throw new IllegalArgumentException("Party size must be between 1 and 10 guests");
        }
        if (request.dateTime() == null || request.dateTime().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Reservation date must be in the future");
        }

        Reservation reservation = new Reservation();
        reservation.setCustomerName(request.customerName());
        reservation.setEmail(request.email());
        reservation.setPhone(request.phone());
        reservation.setDateTime(request.dateTime());
        reservation.setPartySize(request.partySize());
        reservation.setNotes(request.notes());
        reservation.setStatus("CONFIRMED");
        return repository.save(reservation);
    }
}
