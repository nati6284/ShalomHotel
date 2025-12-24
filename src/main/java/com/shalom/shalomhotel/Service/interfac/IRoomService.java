package com.shalom.shalomhotel.Service.interfac;

import com.shalom.shalomhotel.Dto.Response;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IRoomService {
    // Room Type Management
    Response addRoomType(String typeName, String description, BigDecimal pricePerNight,
                         Integer maxCapacity, String amenities, MultipartFile photo);
    Response updateRoomType(Long id, String description, BigDecimal pricePerNight,
                            Integer maxCapacity, String amenities, MultipartFile photo);
    Response deleteRoomType(Long id);
    Response getAllRoomTypes();
    Response getRoomTypeById(Long id);

    // Room Management
    Response addRoom(String roomNumber, Integer floorNumber, Long roomTypeId,
                     Boolean hasView, Boolean isAccessible, String specialFeatures);
    Response updateRoom(Long id, String roomNumber, Integer floorNumber, Long roomTypeId,
                        Boolean hasView, Boolean isAccessible, String specialFeatures);
    Response deleteRoom(Long id);
    Response getAllRooms();
    Response getRoomById(Long id);
    Response updateRoomStatus(Long roomId, String status);

    Response getAvailableRoomsByDatesAndType(LocalDate checkInDate, LocalDate checkOutDate,
                                             String roomType);
    List<String> getAllRoomTypeNames();

}