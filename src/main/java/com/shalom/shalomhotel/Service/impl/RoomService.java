package com.shalom.shalomhotel.Service.impl;

import com.shalom.shalomhotel.Dto.*;
import com.shalom.shalomhotel.Service.AwsS3Service;
import com.shalom.shalomhotel.Service.interfac.IRoomService;
import com.shalom.shalomhotel.entity.*;
import com.shalom.shalomhotel.Exception.OurException;
import com.shalom.shalomhotel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService implements IRoomService {

    @Autowired

    private  RoomTypeRepository roomTypeRepository;
    @Autowired

    private  RoomRepository roomRepository;
    @Autowired

    private  BookingRepository bookingRepository;
    @Autowired

    private  AwsS3Service awsS3Service;

    // Helper method to calculate nights
    private long calculateNights(LocalDate checkIn, LocalDate checkOut) {
        return Period.between(checkIn, checkOut).getDays();
    }
    // ========== ROOM TYPE MANAGEMENT ==========

    @Override
    @Transactional
    public Response addRoomType(String typeName, String description, BigDecimal pricePerNight,
                                Integer maxCapacity, String amenities, MultipartFile photo) {
        Response response = new Response();

        try {
            // Validate input
            if (typeName == null || typeName.isBlank()) {
                response.setMessage("Room type name is required");
                return response;
            }

            if (description == null || description.isBlank()) {
                response.setMessage("Description is required");
                return response;
            }

            if (pricePerNight == null || pricePerNight.compareTo(BigDecimal.ZERO) <= 0) {
                response.setMessage("Valid price per night is required");
                return response;
            }

            if (maxCapacity == null || maxCapacity <= 0) {
                response.setMessage("Valid max capacity is required");
                return response;
            }

            // Check for duplicates (simple exists check)
            if (roomTypeRepository.findByTypeName(typeName.trim()).isPresent()) {
                response.setMessage("Room type '" + typeName + "' already exists");
                return response;
            }

            // Upload photo
            String photoUrl = null;
            if (photo != null && !photo.isEmpty()) {
                photoUrl = awsS3Service.uploadFile(photo);
            }

            // Create room type
            RoomType roomType = new RoomType();
            roomType.setTypeName(typeName.trim());
            roomType.setDescription(description.trim());
            roomType.setPricePerNight(pricePerNight);
            roomType.setMaxCapacity(maxCapacity);
            roomType.setAmenities(amenities != null ? amenities.trim() : null);
            roomType.setPhotoUrl(photoUrl);

            RoomType savedRoomType = roomTypeRepository.save(roomType);

            response.setMessage("Room type added successfully");
            response.setRoomType(mapToRoomTypeDTO(savedRoomType));

        } catch (Exception e) {

            response.setMessage("Failed to add room type: " + e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional
    public Response updateRoomType(Long id, String description, BigDecimal pricePerNight,
                                   Integer maxCapacity, String amenities, MultipartFile photo) {
        Response response = new Response();

        try {
            // Find room type
            RoomType roomType = roomTypeRepository.findById(id)
                    .orElseThrow(() -> new OurException("Room type not found"));

            // Update fields if provided
            if (description != null && !description.isBlank()) {
                roomType.setDescription(description.trim());
            }

            if (pricePerNight != null && pricePerNight.compareTo(BigDecimal.ZERO) > 0) {
                roomType.setPricePerNight(pricePerNight);
            }

            if (maxCapacity != null && maxCapacity > 0) {
                roomType.setMaxCapacity(maxCapacity);
            }

            if (amenities != null) {
                roomType.setAmenities(amenities.trim());
            }

            // Update photo if provided
            if (photo != null && !photo.isEmpty()) {
                String photoUrl = awsS3Service.uploadFile(photo);
                roomType.setPhotoUrl(photoUrl);
            }

            RoomType updatedRoomType = roomTypeRepository.save(roomType);

            response.setMessage("Room type updated successfully");
            response.setRoomType(mapToRoomTypeDTO(updatedRoomType));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to update room type");
        }

        return response;
    }

    @Override
    @Transactional
    public Response deleteRoomType(Long id) {
        Response response = new Response();

        try {
            RoomType roomType = roomTypeRepository.findById(id)
                    .orElseThrow(() -> new OurException("Room type not found"));

            // Check if any rooms are using this type (simple count)
            long roomCount = roomRepository.countByRoomType(roomType);
            if (roomCount > 0) {
                response.setMessage("Cannot delete room type. There are " + roomCount + " rooms using this type.");
                return response;
            }

            roomTypeRepository.delete(roomType);
            response.setMessage("Room type deleted successfully");

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to delete room type");
        }

        return response;
    }

    @Override
    public Response getAllRoomTypes() {
        Response response = new Response();

        try {
            List<RoomType> roomTypes = roomTypeRepository.findAll();
            List<RoomTypeDTO> dtos = roomTypes.stream()
                    .map(this::mapToRoomTypeDTO)
                    .collect(Collectors.toList());

            response.setMessage("Room types retrieved successfully");
            response.setRoomTypeList(dtos);

        } catch (Exception e) {

            response.setMessage("Failed to retrieve room types");
        }

        return response;
    }

    @Override
    public Response getRoomTypeById(Long id) {
        Response response = new Response();

        try {
            RoomType roomType = roomTypeRepository.findById(id)
                    .orElseThrow(() -> new OurException("Room type not found"));

            response.setMessage("Room type retrieved successfully");
            response.setRoomType(mapToRoomTypeDTO(roomType));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to retrieve room type");
        }

        return response;
    }

    // ========== ROOM MANAGEMENT ==========

    @Override
    @Transactional
    public Response addRoom(String roomNumber, Integer floorNumber, Long roomTypeId,
                            Boolean hasView, Boolean isAccessible, String specialFeatures) {
        Response response = new Response();

        try {
            // Validate input
            if (roomNumber == null || roomNumber.isBlank()) {
                response.setMessage("Room number is required");
                return response;
            }

            if (floorNumber == null || floorNumber < 0) {
                response.setMessage("Valid floor number is required");
                return response;
            }

            if (roomTypeId == null) {
                response.setMessage("Room type ID is required");
                return response;
            }

            // Check if room number exists (simple check)
            if (roomRepository.findByRoomNumber(roomNumber.trim()).isPresent()) {
                response.setMessage("Room number '" + roomNumber + "' already exists");
                return response;
            }

            // Get room type
            RoomType roomType = roomTypeRepository.findById(roomTypeId)
                    .orElseThrow(() -> new OurException("Room type not found"));

            // Create room
            Room room = new Room();
            room.setRoomNumber(roomNumber.trim());
            room.setFloorNumber(floorNumber);
            room.setRoomType(roomType);
            room.setStatus(Room.RoomStatus.AVAILABLE);
            room.setHasView(hasView != null ? hasView : false);
            room.setIsAccessible(isAccessible != null ? isAccessible : false);
            room.setSpecialFeatures(specialFeatures != null ? specialFeatures.trim() : null);

            Room savedRoom = roomRepository.save(room);

            response.setMessage("Room added successfully");
            response.setRoom(mapToRoomDTO(savedRoom));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to add room");
        }

        return response;
    }

    @Override
    @Transactional
    public Response updateRoom(Long id, String roomNumber, Integer floorNumber, Long roomTypeId,
                               Boolean hasView, Boolean isAccessible, String specialFeatures) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(id)
                    .orElseThrow(() -> new OurException("Room not found"));

            // Check if updating room number
            if (roomNumber != null && !roomNumber.isBlank()) {
                String trimmedRoomNumber = roomNumber.trim();
                if (!room.getRoomNumber().equals(trimmedRoomNumber)) {
                    // Check if new room number already exists
                    if (roomRepository.findByRoomNumber(trimmedRoomNumber).isPresent()) {
                        response.setMessage("Room number '" + roomNumber + "' already exists");
                        return response;
                    }
                    room.setRoomNumber(trimmedRoomNumber);
                }
            }

            if (floorNumber != null && floorNumber >= 0) {
                room.setFloorNumber(floorNumber);
            }

            if (roomTypeId != null) {
                RoomType roomType = roomTypeRepository.findById(roomTypeId)
                        .orElseThrow(() -> new OurException("Room type not found"));
                room.setRoomType(roomType);
            }

            if (hasView != null) {
                room.setHasView(hasView);
            }

            if (isAccessible != null) {
                room.setIsAccessible(isAccessible);
            }

            if (specialFeatures != null) {
                room.setSpecialFeatures(specialFeatures.trim());
            }

            Room updatedRoom = roomRepository.save(room);

            response.setMessage("Room updated successfully");
            response.setRoom(mapToRoomDTO(updatedRoom));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to update room");
        }

        return response;
    }

    @Override
    @Transactional
    public Response updateRoomStatus(Long roomId, String status) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room not found"));

            try {
                Room.RoomStatus newStatus = Room.RoomStatus.valueOf(status.toUpperCase());

                // Validate status transition
                if (!isValidStatusTransition(room.getStatus(), newStatus)) {
                    response.setMessage("Invalid status transition from " +
                            room.getStatus() + " to " + newStatus);
                    return response;
                }

                room.setStatus(newStatus);
                roomRepository.save(room);

                response.setMessage("Room status updated successfully");
                response.setRoom(mapToRoomDTO(room));

            } catch (IllegalArgumentException e) {
                response.setMessage("Invalid status: " + status);
            }

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to update room status");
        }

        return response;
    }

    private boolean isValidStatusTransition(Room.RoomStatus current, Room.RoomStatus newStatus) {
        // Define valid status transitions
        if (current == newStatus) {
            return true;
        }

        switch (current) {
            case AVAILABLE:
                return newStatus == Room.RoomStatus.RESERVED ||
                        newStatus == Room.RoomStatus.MAINTENANCE;
            case RESERVED:
                return newStatus == Room.RoomStatus.AVAILABLE ||
                        newStatus == Room.RoomStatus.OCCUPIED;
            case OCCUPIED:
                return newStatus == Room.RoomStatus.CLEANING;
            case CLEANING:
                return newStatus == Room.RoomStatus.AVAILABLE;
            case MAINTENANCE:
                return newStatus == Room.RoomStatus.AVAILABLE;
            default:
                return false;
        }
    }

    @Override
    @Transactional
    public Response deleteRoom(Long id) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(id)
                    .orElseThrow(() -> new OurException("Room not found"));

            // Check for active bookings (simplified)
            LocalDate today = LocalDate.now();
            boolean hasActiveBookings = bookingRepository.existsByRoomAndCheckOutDateAfter(room, today);

            if (hasActiveBookings) {
                response.setMessage("Cannot delete room with active or future bookings");
                return response;
            }

            roomRepository.delete(room);
            response.setMessage("Room deleted successfully");

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to delete room");
        }

        return response;
    }

    @Override
    public Response getAllRooms() {
        Response response = new Response();

        try {
            List<Room> rooms = roomRepository.findAll();
            List<RoomDTO> dtos = rooms.stream()
                    .map(this::mapToRoomDTO)
                    .collect(Collectors.toList());

            response.setMessage("Rooms retrieved successfully");
            response.setRoomList(dtos);

        } catch (Exception e) {

            response.setMessage("Failed to retrieve rooms");
        }

        return response;
    }

    @Override
    public Response getRoomById(Long id) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(id)
                    .orElseThrow(() -> new OurException("Room not found"));

            response.setMessage("Room retrieved successfully");
            response.setRoom(mapToRoomDTO(room));

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to retrieve room");
        }

        return response;
    }

    // ========== SEARCH AND AVAILABILITY ==========

    @Override
    public Response getAvailableRoomsByDatesAndType(LocalDate checkInDate, LocalDate checkOutDate,
                                                    String roomType) {
        Response response = new Response();

        try {
            // Validate dates
            if (checkInDate == null || checkOutDate == null) {
                response.setMessage("Check-in and check-out dates are required");
                return response;
            }

            if (checkInDate.isBefore(LocalDate.now())) {
                response.setMessage("Check-in date cannot be in the past");
                return response;
            }

            if (!checkOutDate.isAfter(checkInDate)) {
                response.setMessage("Check-out date must be after check-in date");
                return response;
            }

            if (roomType == null || roomType.isBlank()) {
                response.setMessage("Room type is required");
                return response;
            }

            // Get room type
            RoomType rt = roomTypeRepository.findByTypeName(roomType.trim())
                    .orElseThrow(() -> new OurException("Room type not found"));

            List<Room> availableRooms = findAvailableRooms(rt.getId(), checkInDate, checkOutDate);

            // Create DTOs for individual rooms
            List<RoomDTO> roomDTOs = availableRooms.stream()
                    .map(this::mapToRoomDTO)
                    .collect(Collectors.toList());

            response.setRoomList(roomDTOs);

            if (availableRooms.isEmpty()) {
                response.setMessage("No available rooms for the selected dates");
            } else {
                response.setMessage("Available rooms found");
            }

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {

            response.setMessage("Failed to check availability");
        }

        return response;
    }


    // Replace the findAvailableRooms method with proper implementation
    private List<Room> findAvailableRooms(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate) {
        // Get all rooms of the specified type
        List<Room> rooms = roomRepository.findByRoomTypeId(roomTypeId);

        // Filter rooms that are available for the given dates
        return rooms.stream()
                .filter(room -> room.getStatus() == Room.RoomStatus.AVAILABLE)
                .filter(room -> isRoomAvailableForDates(room, checkInDate, checkOutDate))
                .collect(Collectors.toList());
    }

    private boolean isRoomAvailableForDates(Room room, LocalDate checkIn, LocalDate checkOut) {
        // Check for any overlapping bookings
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                room.getId(),
                checkIn,
                checkOut  // REMOVE .minusDays(1) - use checkOut directly
        );

        return overlappingBookings.isEmpty();
    }



    @Override
    public List<String> getAllRoomTypeNames() {
        return roomTypeRepository.findAll().stream()
                .map(RoomType::getTypeName)
                .collect(Collectors.toList());
    }

    // ========== HELPER METHODS ==========

    private RoomTypeDTO mapToRoomTypeDTO(RoomType roomType) {
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

    private RoomDTO mapToRoomDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setFloorNumber(room.getFloorNumber());
        dto.setStatus(room.getStatus());
        dto.setHasView(room.getHasView());
        dto.setIsAccessible(room.getIsAccessible());
        dto.setSpecialFeatures(room.getSpecialFeatures());

        // Add room type info
        if (room.getRoomType() != null) {
            dto.setRoomTypeName(room.getRoomType().getTypeName());
            dto.setDescription(room.getRoomType().getDescription());
            dto.setPricePerNight(room.getRoomType().getPricePerNight());
            dto.setPhotoUrl(room.getRoomType().getPhotoUrl());
            dto.setMaxCapacity(room.getRoomType().getMaxCapacity());
            dto.setAmenities(room.getRoomType().getAmenities());
        }

        return dto;
    }
}