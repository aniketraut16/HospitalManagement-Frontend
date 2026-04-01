package com.frontend.HospitalManagement.dto.room;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private int roomNumber;
    private String block; // From list view
    private Integer blockFloor; // From single view GET
    private Integer blockCode; // From single view GET
    private String roomType;
    private String status;
    private boolean unavailable;
}
