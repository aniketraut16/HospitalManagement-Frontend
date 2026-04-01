package com.frontend.HospitalManagement.dto.room;

import com.frontend.HospitalManagement.dto.room.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomDTO {
    private RoomType roomType;
    private Integer blockFloor;
    private Integer blockCode;
}
