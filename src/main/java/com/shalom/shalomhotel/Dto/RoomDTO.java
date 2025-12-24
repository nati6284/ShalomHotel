package com.shalom.shalomhotel.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shalom.shalomhotel.entity.Room.RoomStatus;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomDTO {
    private Long id;
    private String roomNumber;
    private Integer floorNumber;
    private RoomStatus status;
    private Boolean hasView;
    private Boolean isAccessible;
    private String specialFeatures;
    private String roomType;
    private Long roomTypeId;
    private String roomTypeName;
    private String description;
    private BigDecimal pricePerNight;
    private Integer maxCapacity;
    private String photoUrl;
    private String amenities;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public Boolean getHasView() { return hasView; }
    public void setHasView(Boolean hasView) { this.hasView = hasView; }

    public Boolean getIsAccessible() { return isAccessible; }
    public void setIsAccessible(Boolean isAccessible) { this.isAccessible = isAccessible; }

    public String getSpecialFeatures() { return specialFeatures; }
    public void setSpecialFeatures(String specialFeatures) { this.specialFeatures = specialFeatures; }

    // For simple room type name
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    // For detailed room type info
    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
}