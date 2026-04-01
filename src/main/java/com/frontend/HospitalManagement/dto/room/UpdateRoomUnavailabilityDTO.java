package com.frontend.HospitalManagement.dto.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoomUnavailabilityDTO {
    Boolean unavailable;
}
