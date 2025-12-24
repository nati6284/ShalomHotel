package com.shalom.shalomhotel.Service.interfac;

import com.shalom.shalomhotel.Dto.BookingRequestDTO;
import com.shalom.shalomhotel.Dto.Response;
import java.time.LocalDate;
import java.util.List;

public interface IBookingService {

    // Create and retrieve bookings
    Response createBooking(BookingRequestDTO bookingRequest);
    Response getBookingByConfirmationCode(String confirmationCode);
    Response getUserBookings(Long userId);
    Response getAllBookings();

    // Booking status management
    Response cancelBooking(String confirmationCode);
    Response confirmBooking(String confirmationCode);

    Response getBookingsByStatus(String status);

    Response searchBookings(String searchTerm);

    Response checkInBooking(String confirmationCode);
    Response checkOutBooking(String confirmationCode);


}