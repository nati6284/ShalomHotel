package com.shalom.shalomhotel.repository;

import com.shalom.shalomhotel.entity.Room;
import com.shalom.shalomhotel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findByRoomTypeId(Long roomTypeId);
    long countByRoomType(RoomType roomType);

}