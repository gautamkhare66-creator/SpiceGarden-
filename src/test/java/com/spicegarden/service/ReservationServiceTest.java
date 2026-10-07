package com.spicegarden.service;

import com.spicegarden.domain.Reservation;
import com.spicegarden.repository.ReservationRepository;
import com.spicegarden.service.dto.ReservationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationServiceTest {

    private ReservationRepository repository;
    private ReservationService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ReservationRepository.class);
        service = new ReservationService(repository);
    }

    @Test
    void createsReservationWithConfirmedStatus() {
        ReservationRequest request = new ReservationRequest(
                "Riya Shah", "riya@example.com", "9123456789",
                LocalDateTime.of(2026, 10, 10, 19, 30), 4, "Window table"
        );
        Mockito.when(repository.save(Mockito.any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        Reservation saved = service.createReservation(request);

        assertEquals(7L, saved.getId());
        assertEquals("CONFIRMED", saved.getStatus());
        assertEquals(4, saved.getPartySize());
    }

    @Test
    void rejectsPartiesLargerThanTen() {
        ReservationRequest request = new ReservationRequest(
                "Riya Shah", "riya@example.com", "9123456789",
                LocalDateTime.of(2026, 10, 10, 19, 30), 11, ""
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.createReservation(request));
    }
}
