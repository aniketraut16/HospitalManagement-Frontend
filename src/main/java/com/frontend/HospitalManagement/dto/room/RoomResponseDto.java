package com.frontend.HospitalManagement.dto.room;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDto {
    private List<RoomDto> rooms;
    private PageDto page;
}
