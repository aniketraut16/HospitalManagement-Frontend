package com.frontend.HospitalManagement.dto.room;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {
    private int size;
    private long totalElements;
    private int totalPages;
    private int number;
}
