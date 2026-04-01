package com.frontend.HospitalManagement.dto.room;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StayResponseDto {
    private List<StayDto> stays;
    private PageDto page;
}
