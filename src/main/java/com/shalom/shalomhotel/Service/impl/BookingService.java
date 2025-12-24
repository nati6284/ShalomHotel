package com.shalom.shalomhotel.Service.impl;

import com.shalom.shalomhotel.Dto.*;
import com.shalom.shalomhotel.Service.interfac.IBookingService;
import com.shalom.shalomhotel.entity.*;
import com.shalom.shalomhotel.Exception.OurException;
import com.shalom.shalomhotel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService implements IBookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomTypeRepository roomTypeRepository;


    private long calculateNights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return 0;
        }
        return Period.between(checkIn, checkOut).getDays();
    }

    @Override
    @Transactional
    public Response createBooking(BookingRequestDTO bookingRequest) {
        Response response = new Response();

        try {
            if (bookingRequest.getUserId() == null) {
                response.setMessage("User ID is required");
                return response;
            }

            if (bookingRequest.getRoomId() == null && bookingRequest.getRoomTypeId() == null) {
                response.setMessage("Either Room ID or Room Type ID is required");
                return response;
            }

            if (bookingRequest.getCheckInDate() == null) {
                response.setMessage("Check-in date is required");
                return response;
            }

            if (bookingRequest.getCheckOutDate() == null) {
                response.setMessage("Check-out date is required");
                return response;
            }

            Integer numberOfGuests = calculateTotalGuests(bookingRequest);
            if (numberOfGuests == null || numberOfGuests <= 0) {
                response.setMessage("Valid number of guests is required");
                return response;
            }

            LocalDate today = LocalDate.now();
            if (bookingRequest.getCheckInDate().isBefore(today)) {
                response.setMessage("Check-in date cannot be in the past");
                return response;
            }

            if (!bookingRequest.getCheckOutDate().isAfter(bookingRequest.getCheckInDate())) {
                response.setMessage("Check-out date must be after check-in date");
                return response;
            }

            User user = userRepository.findById(bookingRequest.getUserId())
                    .orElseThrow(() -> new OurException("User not found"));

            Room room;

            if (bookingRequest.getRoomId() != null) {
                room = roomRepository.findById(bookingRequest.getRoomId())
                        .orElseThrow(() -> new OurException("Room not found with ID: " + bookingRequest.getRoomId()));

                if (!isRoomAvailableForDates(room, bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate())) {
                    response.setMessage("Room " + room.getRoomNumber() + " is not available for the selected dates");
                    return response;
                }

                if (numberOfGuests > room.getRoomType().getMaxCapacity()) {
                    response.setMessage("Number of guests (" + numberOfGuests +
                            ") exceeds room capacity (" + room.getRoomType().getMaxCapacity() + ")");
                    return response;
                }

            } else {
                room = findAvailableRoomByType(bookingRequest, bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
                if (room == null) {
                    response.setMessage("No available rooms of this type for the selected dates");
                    return response;
                }
            }

            long nights = calculateNights(
                    bookingRequest.getCheckInDate(),
                    bookingRequest.getCheckOutDate()
            );

            if (nights == 0) {
                nights = 1;
            }
            BigDecimal totalPrice = room.getRoomType().getPricePerNight()
                    .multiply(BigDecimal.valueOf(nights));

            BigDecimal finalTotalPrice = bookingRequest.getTotalPrice() != null
                    ? bookingRequest.getTotalPrice()
                    : totalPrice;

            Booking booking = new Booking();
            booking.setRoom(room);
            booking.setUser(user);
            booking.setCheckInDate(bookingRequest.getCheckInDate());
            booking.setCheckOutDate(bookingRequest.getCheckOutDate());
            booking.setNumberOfGuests(numberOfGuests);
            booking.setNumOfAdults(bookingRequest.getNumOfAdults());
            booking.setNumOfChildren(bookingRequest.getNumOfChildren());
            booking.setTotalPrice(finalTotalPrice);
            booking.setBookingStatus(Booking.BookingStatus.PENDING);
            booking.setSpecialRequests(bookingRequest.getSpecialRequests());

            Booking savedBooking = bookingRepository.save(booking);


            room.setStatus(Room.RoomStatus.RESERVED);
            roomRepository.save(room);

            response.setMessage("Booking created successfully");
            response.setBooking(mapToBookingDTO(savedBooking));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to create booking");
        }

        return response;
    }

    private Integer calculateTotalGuests(BookingRequestDTO bookingRequest) {
        if (bookingRequest.getNumberOfGuests() != null) {
            return bookingRequest.getNumberOfGuests();
        }


        int adults = bookingRequest.getNumOfAdults() != null ? bookingRequest.getNumOfAdults() : 0;
        int children = bookingRequest.getNumOfChildren() != null ? bookingRequest.getNumOfChildren() : 0;

        int total = adults + children;
        return total > 0 ? total : null;
    }

    private boolean isRoomAvailableForDates(Room room, LocalDate checkIn, LocalDate checkOut) {

        if (room.getStatus() != Room.RoomStatus.AVAILABLE &&
                room.getStatus() != Room.RoomStatus.CLEANING) {

            return false;
        }


        List<Booking> existingBookings = bookingRepository.findByRoomIdAndBookingStatusNotIn(
                room.getId(),
                Arrays.asList(Booking.BookingStatus.CANCELLED, Booking.BookingStatus.CHECKED_OUT)
        );

        for (Booking booking : existingBookings) {

            if (checkIn.isBefore(booking.getCheckOutDate()) &&
                    checkOut.isAfter(booking.getCheckInDate())) {

                return false;
            }
        }

        return true;
    }

    private Room findAvailableRoomByType(BookingRequestDTO bookingRequest, LocalDate checkIn, LocalDate checkOut) {

        List<Room> rooms = roomRepository.findByRoomTypeId(bookingRequest.getRoomTypeId());


        for (Room room : rooms) {
            if (isRoomAvailableForDates(room, checkIn, checkOut)) {

                Integer numberOfGuests = calculateTotalGuests(bookingRequest);
                if (numberOfGuests != null && numberOfGuests <= room.getRoomType().getMaxCapacity()) {
                    return room;
                }
            }
        }

        return null;
    }

    @Override
    public Response getBookingByConfirmationCode(String confirmationCode) {
        Response response = new Response();

        try {
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode)
                    .orElseThrow(() -> new OurException("Booking not found with code: " + confirmationCode));

            response.setMessage("Booking retrieved successfully");
            response.setBooking(mapToBookingDTO(booking));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to retrieve booking");
        }

        return response;
    }

    @Override
    public Response getUserBookings(Long userId) {
        Response response = new Response();

        try {
            List<Booking> bookings = bookingRepository.findByUserId(userId);
            List<BookingDTO> dtos = bookings.stream()
                    .map(this::mapToBookingDTO)
                    .sorted((b1, b2) -> b2.getBookingDate().compareTo(b1.getBookingDate()))
                    .collect(Collectors.toList());

            response.setMessage("Bookings retrieved successfully");
            response.setBookingList(dtos);

        } catch (Exception e) {

            response.setMessage("Failed to retrieve bookings");
        }

        return response;
    }

    @Override
    public Response getAllBookings() {
        Response response = new Response();

        try {
            List<Booking> bookings = bookingRepository.findAll();
            List<BookingDTO> dtos = bookings.stream()
                    .map(this::mapToBookingDTO)
                    .sorted((b1, b2) -> b2.getBookingDate().compareTo(b1.getBookingDate()))
                    .collect(Collectors.toList());

            response.setMessage("All bookings retrieved successfully");
            response.setBookingList(dtos);

        } catch (Exception e) {

            response.setMessage("Failed to retrieve bookings");
        }

        return response;
    }

    @Override
    @Transactional
    public Response cancelBooking(String confirmationCode) {
        Response response = new Response();

        try {
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode)
                    .orElseThrow(() -> new OurException("Booking not found"));


            if (booking.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
                response.setMessage("Booking is already cancelled");
                return response;
            }

            if (booking.getBookingStatus() == Booking.BookingStatus.CHECKED_IN ||
                    booking.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT) {
                response.setMessage("Cannot cancel booking that is already checked in/out");
                return response;
            }


            booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
            booking.setCancellationDate(LocalDateTime.now());
            bookingRepository.save(booking);


            Room room = booking.getRoom();
            if (room.getStatus() == Room.RoomStatus.RESERVED) {
                room.setStatus(Room.RoomStatus.AVAILABLE);
                roomRepository.save(room);
            }

            response.setMessage("Booking cancelled successfully");
            response.setBooking(mapToBookingDTO(booking));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to cancel booking");
        }

        return response;
    }

    @Override
    @Transactional
    public Response confirmBooking(String confirmationCode) {
        Response response = new Response();

        try {
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode)
                    .orElseThrow(() -> new OurException("Booking not found"));

            if (booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
                response.setMessage("Only pending bookings can be confirmed");
                return response;
            }

            booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
            booking.setConfirmationDate(LocalDateTime.now());
            bookingRepository.save(booking);

            response.setMessage("Booking confirmed successfully");
            response.setBooking(mapToBookingDTO(booking));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to confirm booking");
        }

        return response;
    }

    @Override
    @Transactional
    public Response checkInBooking(String confirmationCode) {
        Response response = new Response();

        try {
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode)
                    .orElseThrow(() -> new OurException("Booking not found"));

            // Validate for check-in
            if (booking.getBookingStatus() != Booking.BookingStatus.CONFIRMED &&
                    booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
                response.setMessage("Only confirmed or pending bookings can be checked in");
                return response;
            }

            LocalDate today = LocalDate.now();
            if (booking.getCheckInDate().isAfter(today)) {
                response.setMessage("Cannot check in before check-in date");
                return response;
            }

            booking.setBookingStatus(Booking.BookingStatus.CHECKED_IN);
            bookingRepository.save(booking);

            Room room = booking.getRoom();
            room.setStatus(Room.RoomStatus.OCCUPIED);
            roomRepository.save(room);

            response.setMessage("Check-in successful");
            response.setBooking(mapToBookingDTO(booking));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to check in");
        }

        return response;
    }

    @Override
    @Transactional
    public Response checkOutBooking(String confirmationCode) {
        Response response = new Response();

        try {
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode)
                    .orElseThrow(() -> new OurException("Booking not found"));

            if (booking.getBookingStatus() != Booking.BookingStatus.CHECKED_IN) {
                response.setMessage("Only checked-in bookings can be checked out");
                return response;
            }

            booking.setBookingStatus(Booking.BookingStatus.CHECKED_OUT);
            bookingRepository.save(booking);


            Room room = booking.getRoom();
            room.setStatus(Room.RoomStatus.CLEANING);
            roomRepository.save(room);

            response.setMessage("Check-out successful");
            response.setBooking(mapToBookingDTO(booking));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to check out");
        }

        return response;
    }

    @Override
    public Response getBookingsByStatus(String status) {
        Response response = new Response();

        try {
            Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
            List<Booking> bookings = bookingRepository.findByBookingStatus(bookingStatus);
            List<BookingDTO> dtos = bookings.stream()
                    .map(this::mapToBookingDTO)
                    .collect(Collectors.toList());

            response.setMessage("Bookings retrieved successfully");
            response.setBookingList(dtos);

        } catch (IllegalArgumentException e) {
            response.setMessage("Invalid status: " + status);
        } catch (Exception e) {

            response.setMessage("Failed to retrieve bookings");
        }

        return response;
    }

    @Override
    public Response searchBookings(String searchTerm) {
        Response response = new Response();

        try {

            List<Booking> allBookings = bookingRepository.findAll();
            List<Booking> filteredBookings = allBookings.stream()
                    .filter(booking ->
                            (booking.getBookingConfirmationCode() != null &&
                                    booking.getBookingConfirmationCode().contains(searchTerm)) ||
                                    (booking.getUser().getEmail() != null &&
                                            booking.getUser().getEmail().contains(searchTerm)) ||
                                    (booking.getUser().getName() != null &&
                                            booking.getUser().getName().contains(searchTerm)) ||
                                    (booking.getRoom().getRoomNumber() != null &&
                                            booking.getRoom().getRoomNumber().contains(searchTerm))
                    )
                    .collect(Collectors.toList());

            List<BookingDTO> dtos = filteredBookings.stream()
                    .map(this::mapToBookingDTO)
                    .collect(Collectors.toList());

            response.setMessage("Search completed");
            response.setBookingList(dtos);

        } catch (Exception e) {

            response.setMessage("Failed to search bookings");
        }

        return response;
    }



    // Helper method
    private BookingDTO mapToBookingDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingConfirmationCode(booking.getBookingConfirmationCode());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setBookingDate(booking.getBookingDate());
        dto.setConfirmationDate(booking.getConfirmationDate());
        dto.setCancellationDate(booking.getCancellationDate());
        dto.setSpecialRequests(booking.getSpecialRequests());

        // Calculate nights using Period
        long nights = calculateNights(booking.getCheckInDate(), booking.getCheckOutDate());
        // Ensure at least 1 night
        if (nights == 0 && !booking.getCheckInDate().equals(booking.getCheckOutDate())) {
            nights = 1;
        }
        dto.setNumberOfNights((int) nights);

        // Map room info
        if (booking.getRoom() != null) {
            dto.setRoomId(booking.getRoom().getId());
            dto.setRoomNumber(booking.getRoom().getRoomNumber());
            if (booking.getRoom().getRoomType() != null) {
                dto.setRoomType(booking.getRoom().getRoomType().getTypeName());
            }
        }

        // Map user info
        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
            dto.setUserEmail(booking.getUser().getEmail());
            dto.setUserName(booking.getUser().getName());
        }

        return dto;
    }

}