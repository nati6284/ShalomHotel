package com.shalom.shalomhotel.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    @Column(nullable = false)
    private Integer floorNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column(name = "has_view", nullable = false)
    private Boolean hasView = false;

    @Column(name = "is_accessible", nullable = false)
    private Boolean isAccessible = false;

    @Column(name = "special_features", length = 500)
    private String specialFeatures;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    public enum RoomStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED, CLEANING
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public Boolean getHasView() { return hasView; }
    public void setHasView(Boolean hasView) { this.hasView = hasView; }

    public Boolean getIsAccessible() { return isAccessible; }
    public void setIsAccessible(Boolean isAccessible) { this.isAccessible = isAccessible; }

    public String getSpecialFeatures() { return specialFeatures; }
    public void setSpecialFeatures(String specialFeatures) { this.specialFeatures = specialFeatures; }

    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}