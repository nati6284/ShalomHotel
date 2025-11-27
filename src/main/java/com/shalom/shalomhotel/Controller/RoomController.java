package com.shalom.shalomhotel.Controller;

import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.Service.interfac.IBookingService;
import com.shalom.shalomhotel.Service.interfac.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private IRoomService roomService;
    @Autowired
    private IBookingService iBookingService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> addNewRoom(
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "roomPrice", required = false) BigDecimal roomPrice,
            @RequestParam(value = "roomDescription", required = false) String roomDescription) {

        // Input validation
        if (photo == null || photo.isEmpty() || roomType == null || roomType.isBlank() || roomPrice == null) {
            Response response = new Response();
            response.setMessage("Please provide all required fields: photo, roomType, roomPrice");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400
        }

        Response response = roomService.addNewRoom(photo, roomType, roomPrice, roomDescription);

        // Determine HTTP status based on response content
        if (response.getRoom() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllRooms() {
        Response response = roomService.getAllRooms();

        if (response.getRoomList() != null && !response.getRoomList().isEmpty()) {
            return ResponseEntity.ok(response); // 200
        } else if (response.getMessage() != null && response.getMessage().contains("No rooms")) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response); // 204
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500
        }
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getAllRoomTypes() {
        List<String> roomTypes = roomService.getAllRoomType();
        return ResponseEntity.ok(roomTypes); // 200
    }

    @GetMapping("/room-by-id/{roomId}")
    public ResponseEntity<Response> getRoomById(@PathVariable Long roomId) {
        Response response = roomService.getRoomById(roomId);

        if (response.getRoom() != null) {
            return ResponseEntity.ok(response); // 200
        } else if (response.getMessage() != null && response.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500
        }
    }

    @GetMapping("/all-available-room")
    public ResponseEntity<Response> getAvailableRooms() {
        Response response = roomService.getAllAvailableRooms();

        if (response.getRoomList() != null && !response.getRoomList().isEmpty()) {
            return ResponseEntity.ok(response); // 200
        } else if (response.getMessage() != null && response.getMessage().contains("No available")) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response); // 204
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500
        }
    }

    @GetMapping("/available-rooms-by-date-and-type")
    public ResponseEntity<Response> getAvailableRoomsByDateAndType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) String roomType) {

        // Input validation
        if (checkInDate == null || checkOutDate == null || roomType == null || roomType.isBlank()) {
            Response response = new Response();
            response.setMessage("Please provide all required fields: checkInDate, checkOutDate, roomType");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400
        }

        // Validate dates
        if (checkOutDate.isBefore(checkInDate) || checkInDate.isBefore(LocalDate.now())) {
            Response response = new Response();
            response.setMessage("Invalid dates: check-in must be today or later, and check-out must be after check-in");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400
        }

        Response response = roomService.getAvailableRoomsByDataAndType(checkInDate, checkOutDate, roomType);

        if (response.getRoomList() != null && !response.getRoomList().isEmpty()) {
            return ResponseEntity.ok(response); // 200
        } else if (response.getMessage() != null && response.getMessage().contains("No available")) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response); // 204
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400
        }
    }

    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateRoom(
            @PathVariable Long roomId,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "roomPrice", required = false) BigDecimal roomPrice,
            @RequestParam(value = "roomDescription", required = false) String roomDescription) {

        Response response = roomService.updateRoom(roomId, roomDescription, roomType, roomPrice, photo);

        if (response.getRoom() != null) {
            return ResponseEntity.ok(response); // 200
        } else if (response.getMessage() != null && response.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400
        }
    }

    @DeleteMapping("/delete/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteRoom(@PathVariable Long roomId) {
        Response response = roomService.deleteRoom(roomId);

        if (response.getMessage() != null && response.getMessage().contains("deleted successfully")) {
            return ResponseEntity.ok(response); // 200
        } else if (response.getMessage() != null && response.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500
        }
    }
}