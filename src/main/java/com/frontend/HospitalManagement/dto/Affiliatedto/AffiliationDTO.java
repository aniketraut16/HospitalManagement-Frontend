package com.frontend.HospitalManagement.dto.Affiliatedto;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffiliationDTO {
    private Boolean primaryAffiliation;
    private String doctorName;
    private String doctorPosition;
    private String departmentName;
    private String departmentHeadName;
}

