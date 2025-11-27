package com.shalom.shalomhotel.Service.impl;

import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.Dto.RoomDTO;
import com.shalom.shalomhotel.Exception.OurException;
import com.shalom.shalomhotel.Service.LocalFileStorageService;
import com.shalom.shalomhotel.Service.interfac.IRoomService;
import com.shalom.shalomhotel.entity.Room;
import com.shalom.shalomhotel.repository.BookingRepository;
import com.shalom.shalomhotel.repository.RoomRepository;
import com.shalom.shalomhotel.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RomService implements IRoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private LocalFileStorageService localFileStorageService;

    @Override
    public Response addNewRoom(MultipartFile photo, String roomType, BigDecimal roomPrice, String description) {
        Response response = new Response();

        try {
            String imageUrl = localFileStorageService.saveImageToLocal(photo);
            Room room = new Room();
            room.setRoomPhotoUrl(imageUrl);
            room.setRoomType(roomType);
            room.setRoomPrice(roomPrice);
            room.setRoomDescription(description);
            Room savedRoom = roomRepository.save(room);
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(savedRoom);

            response.setMessage("Room added successfully");
            response.setRoom(roomDTO);

        } catch (Exception e) {
            response.setMessage("Error adding room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public List<String> getAllRoomType() {
        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public Response getAllRooms() {
        Response response = new Response();

        try {
            List<Room> roomList = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            if (roomDTOList.isEmpty()) {
                response.setMessage("No rooms found");
            } else {
                response.setMessage("Rooms retrieved successfully");
                response.setRoomList(roomDTOList);
            }

        } catch (Exception e) {
            response.setMessage("Error retrieving rooms: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response deleteRoom(Long roomId) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room not found"));
            roomRepository.deleteById(roomId);
            response.setMessage("Room deleted successfully");

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error deleting room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response updateRoom(Long roomId, String description, String roomType, BigDecimal roomPrice, MultipartFile photo) {
        Response response = new Response();

        try {
            String imageUrl = null;
            if (photo != null && !photo.isEmpty()) {
                imageUrl = localFileStorageService.saveImageToLocal(photo);
            }

            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room not found"));

            if (roomType != null) room.setRoomType(roomType);
            if (roomPrice != null) room.setRoomPrice(roomPrice);
            if (description != null) room.setRoomDescription(description);
            if (imageUrl != null) room.setRoomPhotoUrl(imageUrl);

            Room updatedRoom = roomRepository.save(room);
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(updatedRoom);

            response.setMessage("Room updated successfully");
            response.setRoom(roomDTO);

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error updating room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getRoomById(Long roomId) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room not found"));
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTOPlusBookings(room);

            response.setMessage("Room retrieved successfully");
            response.setRoom(roomDTO);

        } catch (OurException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error retrieving room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAvailableRoomsByDataAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        Response response = new Response();

        try {
            List<Room> availableRooms = roomRepository.findAvailableRoomsByDatesAndTypes(checkInDate, checkOutDate, roomType);
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(availableRooms);

            if (roomDTOList.isEmpty()) {
                response.setMessage("No available rooms found for the selected criteria");
            } else {
                response.setMessage("Available rooms retrieved successfully");
                response.setRoomList(roomDTOList);
            }

        } catch (Exception e) {
            response.setMessage("Error retrieving available rooms: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAllAvailableRooms() {
        Response response = new Response();

        try {
            List<Room> roomList = roomRepository.getAllAvailableRooms();
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            if (roomDTOList.isEmpty()) {
                response.setMessage("No available rooms found");
            } else {
                response.setMessage("Available rooms retrieved successfully");
                response.setRoomList(roomDTOList);
            }

        } catch (Exception e) {
            response.setMessage("Error retrieving available rooms: " + e.getMessage());
        }
        return response;
    }
}