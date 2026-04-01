package com.frontend.HospitalManagement.dto.room;


import com.frontend.HospitalManagement.dto.room.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomDTO {
    private Integer roomNumber;
    private RoomType roomType;
    private Integer blockFloor;
    private Integer blockCode;
    private Boolean unavailable;
}