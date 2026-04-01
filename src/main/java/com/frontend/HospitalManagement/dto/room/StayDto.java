package com.frontend.HospitalManagement.dto.room;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StayDto {
    private String status;
    private String patientName;
    private String stayStart;
    private String stayEnd;
}
