package com.shalom.shalomhotel.utils;

import com.shalom.shalomhotel.Dto.*;
import com.shalom.shalomhotel.entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    // Room mapping methods
    public static RoomDTO mapRoomEntityToRoomDTO(Room room) {
        if (room == null) return null;

      RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setFloorNumber(room.getFloorNumber());
        dto.setStatus(room.getStatus());
        dto.setHasView(room.getHasView());
        dto.setIsAccessible(room.getIsAccessible());
        dto.setSpecialFeatures(room.getSpecialFeatures());

        // Map room type info
        if (room.getRoomType() != null) {
            dto.setRoomType(room.getRoomType().getTypeName()); // Simple string
            dto.setRoomTypeId(room.getRoomType().getId());
            dto.setRoomTypeName(room.getRoomType().getTypeName());
            dto.setDescription(room.getRoomType().getDescription());
            dto.setPricePerNight(room.getRoomType().getPricePerNight());
            dto.setMaxCapacity(room.getRoomType().getMaxCapacity());
            dto.setPhotoUrl(room.getRoomType().getPhotoUrl());
            dto.setAmenities(room.getRoomType().getAmenities());
        }

        return dto;
    }

    public static RoomTypeDTO mapRoomTypeEntityToRoomTypeDTO(RoomType roomType) {
        if (roomType == null) return null;

        RoomTypeDTO dto = new RoomTypeDTO();
        dto.setId(roomType.getId());
        dto.setTypeName(roomType.getTypeName());
        dto.setDescription(roomType.getDescription());
        dto.setPricePerNight(roomType.getPricePerNight());
        dto.setPhotoUrl(roomType.getPhotoUrl());
        dto.setMaxCapacity(roomType.getMaxCapacity());
        dto.setAmenities(roomType.getAmenities());

        return dto;
    }

    // User mapping methods
    public static UserDTO mapUserEntityToUserDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        return dto;
    }

    // Booking mapping methods
    public static BookingDTO mapBookingEntityToBookingDTO(Booking booking) {
        if (booking == null) return null;

        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingConfirmationCode(booking.getBookingConfirmationCode());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setSpecialRequests(booking.getSpecialRequests());
        dto.setBookingDate(booking.getBookingDate());
        dto.setConfirmationDate(booking.getConfirmationDate());
        dto.setCancellationDate(booking.getCancellationDate());

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

        // Calculate number of nights
        if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
            long nights = booking.getCheckOutDate().toEpochDay() - booking.getCheckInDate().toEpochDay();
            dto.setNumberOfNights((int) Math.max(nights, 1)); // At least 1 night
        } else {
            dto.setNumberOfNights(0);
        }

        return dto;
    }

    // AvailableRoomDTO mapping
    public static AvailableRoomDTO mapRoomTypeToAvailableRoomDTO(RoomType roomType,
                                                                 LocalDate checkInDate,
                                                                 LocalDate checkOutDate,
                                                                 Integer availableCount) {
        if (roomType == null) return null;

        AvailableRoomDTO dto = new AvailableRoomDTO();
        dto.setRoomTypeId(roomType.getId());
        dto.setTypeName(roomType.getTypeName());
        dto.setDescription(roomType.getDescription());
        dto.setPricePerNight(roomType.getPricePerNight());
        dto.setPhotoUrl(roomType.getPhotoUrl());
        dto.setMaxCapacity(roomType.getMaxCapacity());
        dto.setAmenities(roomType.getAmenities());
        dto.setAvailableCount(availableCount != null ? availableCount : 0);
        dto.setCheckInDate(checkInDate);
        dto.setCheckOutDate(checkOutDate);

        // Calculate total price
        if (checkInDate != null && checkOutDate != null && roomType.getPricePerNight() != null) {
            long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();
            nights = Math.max(nights, 1); // At least 1 night
            BigDecimal totalPrice = roomType.getPricePerNight().multiply(BigDecimal.valueOf(nights));
            dto.setTotalPrice(totalPrice);
            dto.setNumberOfNights(nights);
        } else {
            dto.setTotalPrice(BigDecimal.ZERO);
            dto.setNumberOfNights(0L);
        }

        return dto;
    }

    // List mapping methods
    public static List<RoomDTO> mapRoomListEntityToRoomListDTO(List<Room> roomList) {
        if (roomList == null || roomList.isEmpty()) return List.of();
        return roomList.stream()
                .map(Utils::mapRoomEntityToRoomDTO)
                .collect(Collectors.toList());
    }

    public static List<RoomTypeDTO> mapRoomTypeListEntityToRoomTypeListDTO(List<RoomType> roomTypeList) {
        if (roomTypeList == null || roomTypeList.isEmpty()) return List.of();
        return roomTypeList.stream()
                .map(Utils::mapRoomTypeEntityToRoomTypeDTO)
                .collect(Collectors.toList());
    }

    public static List<UserDTO> mapUserListEntityToUserListDTO(List<User> userList) {
        if (userList == null || userList.isEmpty()) return List.of();
        return userList.stream()
                .map(Utils::mapUserEntityToUserDTO)
                .collect(Collectors.toList());
    }

    public static List<BookingDTO> mapBookingListEntityToBookingListDTO(List<Booking> bookingList) {
        if (bookingList == null || bookingList.isEmpty()) return List.of();
        return bookingList.stream()
                .map(Utils::mapBookingEntityToBookingDTO)
                .collect(Collectors.toList());
    }

    // Enhanced mapping with rooms count
    public static RoomTypeDTO mapRoomTypeEntityToRoomTypeDTOWithCounts(RoomType roomType) {
        RoomTypeDTO dto = mapRoomTypeEntityToRoomTypeDTO(roomType);
        if (dto != null && roomType.getRooms() != null) {
            int totalRooms = roomType.getRooms().size();
            int availableRooms = (int) roomType.getRooms().stream()
                    .filter(r -> r.getStatus() == Room.RoomStatus.AVAILABLE)
                    .count();

            // Note: Your RoomTypeDTO needs these fields. If not, create a separate DTO
            // dto.setTotalRooms(totalRooms);
            // dto.setAvailableRooms(availableRooms);
        }
        return dto;
    }
}