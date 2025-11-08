package com.track.moneta.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Size(min = 3, max = 30)
    private String name;

    @Email
    private String email;

    @Size(min = 6)
    private String password;

    @Min(value = 1, message = "Budget start date must be at least 1")
    @Max(value = 31, message = "Budget start date cannot exceed 31")
    private Integer budgetStartDate;
}