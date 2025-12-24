package com.shalom.shalomhotel.Controller;

import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.Service.interfac.IRoomService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")

public class RoomController {

    @Autowired
    private IRoomService roomService;

    @PostMapping("/addtypes")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> addRoomType(
            @RequestParam("typeName") String typeName,
            @RequestParam("description") String description,
            @RequestParam("pricePerNight") BigDecimal pricePerNight,
            @RequestParam("maxCapacity") Integer maxCapacity,
            @RequestParam(value = "amenities", required = false) String amenities,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        Response response = roomService.addRoomType(typeName, description, pricePerNight,
                maxCapacity, amenities, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/updatetypes/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateRoomType(
            @PathVariable Long id,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "pricePerNight", required = false) BigDecimal pricePerNight,
            @RequestParam(value = "maxCapacity", required = false) Integer maxCapacity,
            @RequestParam(value = "amenities", required = false) String amenities,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        Response response = roomService.updateRoomType(id, description, pricePerNight,
                maxCapacity, amenities, photo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alltypes")

    public ResponseEntity<Response> getAllRoomTypes() {
        Response response = roomService.getAllRoomTypes();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/types/names")

    public ResponseEntity<List<String>> getAllRoomTypeNames() {
        List<String> roomTypes = roomService.getAllRoomTypeNames();
        return ResponseEntity.ok(roomTypes);
    }

    @GetMapping("/types/{id}")
    public ResponseEntity<Response> getRoomTypeById(@PathVariable Long id) {
        Response response = roomService.getRoomTypeById(id);

        if (response.getRoomType() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/deletetypes/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteRoomType(@PathVariable Long id) {
        Response response = roomService.deleteRoomType(id);

        if (response.getMessage().contains("successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/addroom")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> addRoom(
            @RequestParam("roomNumber") String roomNumber,
            @RequestParam("floorNumber") Integer floorNumber,
            @RequestParam("roomTypeId") Long roomTypeId,
            @RequestParam(value = "hasView", required = false, defaultValue = "false") Boolean hasView,
            @RequestParam(value = "isAccessible", required = false, defaultValue = "false") Boolean isAccessible,
            @RequestParam(value = "specialFeatures", required = false) String specialFeatures,
            HttpServletRequest request) { // Add this parameter


        request.getParameterMap().forEach((key, values) -> {
            System.out.println(key + ": " + Arrays.toString(values));
        });


        Response response = roomService.addRoom(roomNumber, floorNumber, roomTypeId,
                hasView, isAccessible, specialFeatures);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/updateRoom/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateRoom(
            @PathVariable Long id,
            @RequestParam(value = "roomNumber", required = false) String roomNumber,
            @RequestParam(value = "floorNumber", required = false) Integer floorNumber,
            @RequestParam(value = "roomTypeId", required = false) Long roomTypeId,
            @RequestParam(value = "hasView", required = false) Boolean hasView,
            @RequestParam(value = "isAccessible", required = false) Boolean isAccessible,
            @RequestParam(value = "specialFeatures", required = false) String specialFeatures) {

        Response response = roomService.updateRoom(id, roomNumber, floorNumber, roomTypeId,
                hasView, isAccessible, specialFeatures);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updatestatus/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateRoomStatus(
            @PathVariable Long id,
            @RequestParam("status") String status) {

        Response response = roomService.updateRoomStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")

    public ResponseEntity<Response> getAllRooms() {
        Response response = roomService.getAllRooms();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Response> getRoomById(@PathVariable Long id) {
        Response response = roomService.getRoomById(id);

        if (response.getRoom() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteRoom(@PathVariable Long id) {
        Response response = roomService.deleteRoom(id);

        if (response.getMessage().contains("successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    @GetMapping("/availability")
    public ResponseEntity<Response> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam String roomType) {

        System.out.println("Controller received request: " +
                "checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", roomType=" + roomType);

        Response response = roomService.getAvailableRoomsByDatesAndType(checkInDate, checkOutDate, roomType);

        System.out.println("Controller sending response: " + response.getMessage());
        System.out.println("Room list size: " + (response.getRoomList() != null ? response.getRoomList().size() : 0));

        if (response.getRoomList() != null && !response.getRoomList().isEmpty()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        }
    }



}