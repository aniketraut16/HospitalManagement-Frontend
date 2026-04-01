package com.frontend.HospitalManagement.dto.Departmentdto;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {
    private Integer departmentId;
    private String name;
    private String headName;
}

