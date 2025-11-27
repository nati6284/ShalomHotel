package com.shalom.shalomhotel.Service.interfac;

import com.fasterxml.classmate.members.ResolvedParameterizedMember;
import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.entity.Booking;

public interface IBookingService {

    Response saveBooking(Long roomId, Long userId, Booking bookingReuest);

    Response findBookingConfirmationCode(String confirmationCode);

    Response getAllBookings();

    Response cancelBooking(Long bookingId);

}
