package com.frontend.HospitalManagement.dto.Physiciandto;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicianDTO {
    private Integer employeeId;
    private String name;
    private String position;
    private Integer ssn;
}

