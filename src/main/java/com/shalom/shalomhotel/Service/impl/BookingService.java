package com.shalom.shalomhotel.Service.impl;

import com.shalom.shalomhotel.Dto.BookingDTO;
import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.Exception.OurException;
import com.shalom.shalomhotel.Service.interfac.IBookingService;
import com.shalom.shalomhotel.Service.interfac.IRoomService;
import com.shalom.shalomhotel.entity.Booking;
import com.shalom.shalomhotel.entity.Room;
import com.shalom.shalomhotel.entity.User;
import com.shalom.shalomhotel.repository.BookingRepository;
import com.shalom.shalomhotel.repository.RoomRepository;
import com.shalom.shalomhotel.repository.UserRepository;
import com.shalom.shalomhotel.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService implements IBookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private IRoomService roomService;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Response saveBooking(Long roomId, Long userId, Booking bookingRequest) {
        Response response = new Response();

        try {
            if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
                throw new IllegalArgumentException("Check out date must be after check in date");
            }

            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room Not Found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User Not Found"));

            List<Booking> existingBookings = room.getBookings();
            if (!roomIsAvailable(bookingRequest, existingBookings)) {
                throw new OurException("Room not available for selected date range");
            }

            bookingRequest.setRoom(room);
            bookingRequest.setUser(user);
            String bookingConfirmationCode = Utils.generateRandomConfirmationCode(10);
            bookingRequest.setBookingConfirmationCode(bookingConfirmationCode);
            bookingRepository.save(bookingRequest);

            response.setMessage("Booking created successfully");
            response.setBookingConfirmationCode(bookingConfirmationCode);
            response.setBooking(Utils.mapBookingEntityToBookingDTOPlusBookedRooms(bookingRequest, true));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (IllegalArgumentException e) {
            response.setMessage("Invalid date: " + e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error saving booking: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response findBookingConfirmationCode(String confirmationCode) {
        Response response = new Response();

        try {
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode)
                    .orElseThrow(() -> new OurException("Booking Not Found"));
            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRooms(booking, true);
            response.setMessage("Booking found successfully");
            response.setBooking(bookingDTO);

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error finding booking confirmation code: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response getAllBookings() {
        Response response = new Response();

        try {
            List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<BookingDTO> bookingDTOList = Utils.mapBookingListEntityToBookingListDTO(bookingList);

            if (bookingDTOList.isEmpty()) {
                response.setMessage("No bookings found");
            } else {
                response.setMessage("Bookings retrieved successfully");
                response.setBookingList(bookingDTOList);
            }

        } catch (Exception e) {
            response.setMessage("Error getting all bookings: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response cancelBooking(Long bookingId) {
        Response response = new Response();

        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new OurException("Booking not found"));
            bookingRepository.deleteById(bookingId);
            response.setMessage("Booking cancelled successfully");

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error cancelling booking: " + e.getMessage());
        }

        return response;
    }



    private boolean roomIsAvailable(Booking bookingRequest, List<Booking> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate())
                                || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
                                || (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate())
                                && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()))
                                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())
                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate()))
                                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())
                                && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate()))
                                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate()))
                                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))
                );
    }
}