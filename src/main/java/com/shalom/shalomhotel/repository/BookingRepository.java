package com.shalom.shalomhotel.repository;

import com.shalom.shalomhotel.entity.Booking;
import com.shalom.shalomhotel.entity.Booking.BookingStatus;
import com.shalom.shalomhotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingConfirmationCode(String bookingConfirmationCode);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByRoomIdAndBookingStatusNotIn(Long roomId, List<Booking.BookingStatus> statuses);
    List<Booking> findByBookingStatus(BookingStatus status);
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.bookingStatus NOT IN ('CANCELLED', 'CHECKED_OUT') " +
            "AND ((b.checkInDate <= :checkOutDate) AND (b.checkOutDate >= :checkInDate))")
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
    boolean existsByRoomAndCheckOutDateAfter(Room room, LocalDate date);
}