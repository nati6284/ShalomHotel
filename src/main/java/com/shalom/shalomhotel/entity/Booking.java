package com.shalom.shalomhotel.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String bookingConfirmationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private Integer numberOfGuests;

    private Integer numOfAdults;
    private Integer numOfChildren;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime bookingDate = LocalDateTime.now();
    @Column(length = 500)
    private String specialRequests;

    private LocalDateTime confirmationDate;

    private LocalDateTime cancellationDate;

    @PrePersist
    private void generateConfirmationCode() {
        if (this.bookingConfirmationCode == null) {
            this.bookingConfirmationCode = generateUniqueConfirmationCode();
        }
    }

    private String generateUniqueConfirmationCode() {
        String datePart = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = generateRandomAlphanumeric(6).toUpperCase();
        return "SHL-" + datePart + "-" + randomPart;
    }

    private String generateRandomAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, CHECKED_IN, CHECKED_OUT, NO_SHOW
    }

    // Getters and Setters for ALL fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingConfirmationCode() { return bookingConfirmationCode; }
    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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