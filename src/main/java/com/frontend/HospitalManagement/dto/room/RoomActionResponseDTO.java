package com.frontend.HospitalManagement.dto.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomActionResponseDTO {
    private Boolean success;
    private String message;
}
