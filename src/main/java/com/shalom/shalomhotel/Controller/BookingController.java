package com.shalom.shalomhotel.Controller;

import com.shalom.shalomhotel.Dto.BookingRequestDTO;
import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.Service.interfac.IBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private IBookingService bookingService;
    @PostMapping
    public ResponseEntity<Response> createBooking(@RequestBody BookingRequestDTO bookingRequest) {
        Response response = bookingService.createBooking(bookingRequest);

        if (response.getBooking() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<Response> getBookingByConfirmationCode(
            @PathVariable String confirmationCode) {

        Response response = bookingService.getBookingByConfirmationCode(confirmationCode);

        if (response.getBooking() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Response> getUserBookings(@PathVariable Long userId) {
        Response response = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/cancel/{confirmationCode}")
    public ResponseEntity<Response> cancelBooking(@PathVariable String confirmationCode) {
        Response response = bookingService.cancelBooking(confirmationCode);

        if (response.getBooking() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/confirm/{confirmationCode}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> confirmBooking(@PathVariable String confirmationCode) {
        Response response = bookingService.confirmBooking(confirmationCode);

        if (response.getBooking() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> searchBookings(@RequestParam String searchTerm) {
        Response response = bookingService.searchBookings(searchTerm);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllBookings() {
        Response response = bookingService.getAllBookings();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getBookingsByStatus(@PathVariable String status) {
        Response response = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/check-in/{confirmationCode}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> checkInBooking(@PathVariable String confirmationCode) {
        Response response = bookingService.checkInBooking(confirmationCode);

        if (response.getBooking() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/check-out/{confirmationCode}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> checkOutBooking(@PathVariable String confirmationCode) {
        Response response = bookingService.checkOutBooking(confirmationCode);

        if (response.getBooking() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}