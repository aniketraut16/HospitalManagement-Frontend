package com.frontend.HospitalManagement.dto.Physiciandto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicianDTO {
    
    @NotNull(message = "Employee ID is required")
    @Min(value = 1, message = "Employee ID must be greater than 0")
    private Integer employeeId;

    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[A-Za-z\\s\\.]+$", message = "Name must contain only letters, spaces, and dots")
    private String name;

    @NotBlank(message = "Position is required")
    @Pattern(regexp = "^[A-Za-z\\s\\-\\,\\/]+$", message = "Position contains invalid characters")
    private String position;

    @NotNull(message = "SSN is required")
    @Max(value = 999999999, message = "SSN must be at most 9 digits")
    private Long ssn;
}

