package com.frontend.HospitalManagement.dto.Medication;

import lombok.Data;

@Data
public class PrescriptionDTO {

    private String physicianName;
    private String patientName;
    private String dose;
    private String date;
}
