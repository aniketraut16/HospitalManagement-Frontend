package com.frontend.HospitalManagement.dto.Nurse;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NurseDTO {

    @NotNull(message = "Employee ID is required")
    @Min(value = 1, message = "Employee ID must be a positive number")
    @Max(value = 99999, message = "Employee ID cannot exceed 99999")
    private Integer employeeId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Name can only contain letters, spaces, dots, hyphens and apostrophes")
    private String name;

    @NotNull(message = "Position is required")
    private NursePosition position;

    private boolean registered;

    private String availability;

    @NotNull(message = "SSN is required")
    @Min(value = 1, message = "SSN must be a positive number")
    @Max(value = 999999999, message = "SSN cannot exceed 999999999")
    private Integer ssn;
}
