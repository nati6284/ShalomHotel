package com.shalom.shalomhotel.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shalom.shalomhotel.entity.Booking.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDTO {
    private Long id;
    private String bookingConfirmationCode;
    private Long roomId;
    private String roomNumber;
    private String roomType;
    private Long userId;
    private String userEmail;
    private String userName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;

    private Integer numOfAdults;
    private Integer numOfChildren;
    private BigDecimal totalPrice;
    private BookingStatus bookingStatus;
    private String specialRequests;
    private LocalDateTime bookingDate;
    private LocalDateTime confirmationDate;
    private LocalDateTime cancellationDate;
    private Integer numberOfNights;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingConfirmationCode() { return bookingConfirmationCode; }
    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public LocalDateTime getConfirmationDate() { return confirmationDate; }
    public void setConfirmationDate(LocalDateTime confirmationDate) { this.confirmationDate = confirmationDate; }

    public LocalDateTime getCancellationDate() { return cancellationDate; }
    public void setCancellationDate(LocalDateTime cancellationDate) { this.cancellationDate = cancellationDate; }

    public Integer getNumberOfNights() { return numberOfNights; }
    public void setNumberOfNights(Integer numberOfNights) { this.numberOfNights = numberOfNights; }

    public Integer getNumOfAdults() {
        return numOfAdults;
    }

    public void setNumOfAdults(Integer numOfAdults) {
        this.numOfAdults = numOfAdults;
    }

    public Integer getNumOfChildren() {
        return numOfChildren;
    }

    public void setNumOfChildren(Integer numOfChildren) {
        this.numOfChildren = numOfChildren;
    }
}