package com.shalom.shalomhotel.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private String message;
    private String statusCode;
    private String bookingConfirmationCode;
    private String token;
    private String role;
    private String expirationTime;

    private UserDTO user;
    private List<UserDTO> userList;
    private RoomDTO room;
    private List<RoomDTO> roomList;
    private RoomTypeDTO roomType;
    private List<RoomTypeDTO> roomTypeList;
    private List<AvailableRoomDTO> availableRoomList;
    private BookingDTO booking;
    private List<BookingDTO> bookingList;

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public String getBookingConfirmationCode() { return bookingConfirmationCode; }
    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getExpirationTime() { return expirationTime; }
    public void setExpirationTime(String expirationTime) { this.expirationTime = expirationTime; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public List<UserDTO> getUserList() { return userList; }
    public void setUserList(List<UserDTO> userList) { this.userList = userList; }

    public RoomDTO getRoom() { return room; }
    public void setRoom(RoomDTO room) { this.room = room; }

    public List<RoomDTO> getRoomList() { return roomList; }
    public void setRoomList(List<RoomDTO> roomList) { this.roomList = roomList; }

    public RoomTypeDTO getRoomType() { return roomType; }
    public void setRoomType(RoomTypeDTO roomType) { this.roomType = roomType; }

    public List<RoomTypeDTO> getRoomTypeList() { return roomTypeList; }
    public void setRoomTypeList(List<RoomTypeDTO> roomTypeList) { this.roomTypeList = roomTypeList; }

    public List<AvailableRoomDTO> getAvailableRoomList() { return availableRoomList; }
    public void setAvailableRoomList(List<AvailableRoomDTO> availableRoomList) {
        this.availableRoomList = availableRoomList;
    }

    public BookingDTO getBooking() { return booking; }
    public void setBooking(BookingDTO booking) { this.booking = booking; }

    public List<BookingDTO> getBookingList() { return bookingList; }
    public void setBookingList(List<BookingDTO> bookingList) { this.bookingList = bookingList; }
}