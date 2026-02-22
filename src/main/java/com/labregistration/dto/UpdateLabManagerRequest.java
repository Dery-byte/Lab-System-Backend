package com.labregistration.dto;

import com.labregistration.model.Department;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLabManagerRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    private String email;
    private String phoneNumber;
    private String employeeId;


    private String password;
//    private Department department;

    private String department; // <-- Change to String

    // Add whatever fields make sense to update
    // typically you'd exclude password here
}