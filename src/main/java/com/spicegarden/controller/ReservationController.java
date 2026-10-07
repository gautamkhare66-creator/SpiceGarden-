package com.spicegarden.controller;

import com.spicegarden.service.ReservationService;
import com.spicegarden.service.dto.ReservationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reserve")
    public String reservationForm() {
        return "reservation";
    }

    @PostMapping("/reservations")
    public String createReservation(ReservationRequest request, ModelMap model) {
        try {
            model.addAttribute("reservation", reservationService.createReservation(request));
            return "reservation-confirmation";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            return "reservation";
        }
    }
}
