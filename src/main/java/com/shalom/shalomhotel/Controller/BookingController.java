package com.shalom.shalomhotel.Controller;

import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.Service.interfac.IBookingService;
import com.shalom.shalomhotel.entity.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private IBookingService bookingService;

    @PostMapping("/book-room/{roomId}/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Response> saveBookings(@PathVariable Long roomId,
                                                 @PathVariable Long userId,
                                                 @RequestBody Booking bookingRequest) {
        Response response = bookingService.saveBooking(roomId, userId, bookingRequest);

        // Determine HTTP status based on response content
        if (response.getBookingConfirmationCode() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 - Booking created
        } else if (response.getMessage() != null &&
                (response.getMessage().contains("not available") ||
                        response.getMessage().contains("Invalid date"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 - Bad request
        } else if (response.getMessage() != null &&
                response.getMessage().contains("Not Found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 - Room/User not found
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 - Server error
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllBookings() {
        Response response = bookingService.getAllBookings();

        if (response.getBookingList() != null && !response.getBookingList().isEmpty()) {
            return ResponseEntity.ok(response); // 200 - Success with data
        } else if (response.getMessage() != null && response.getMessage().contains("No bookings")) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response); // 204 - No content
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 - Server error
        }
    }

    @GetMapping("/get-by-confirmation-code/{confirmationCode}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Response> getBookingByConfirmationCode(@PathVariable String confirmationCode) {
        Response response = bookingService.findBookingConfirmationCode(confirmationCode);

        if (response.getBooking() != null) {
            return ResponseEntity.ok(response); // 200 - Success with booking data
        } else if (response.getMessage() != null && response.getMessage().contains("Not Found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 - Booking not found
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 - Server error
        }
    }

    @DeleteMapping("/cancel/{bookingId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Response> deleteBooking(@PathVariable Long bookingId) {
        Response response = bookingService.cancelBooking(bookingId);

        if (response.getMessage() != null && response.getMessage().contains("cancelled successfully")) {
            return ResponseEntity.ok(response); // 200 - Successfully cancelled
        } else if (response.getMessage() != null && response.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 - Booking not found
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 - Server error
        }
    }


}