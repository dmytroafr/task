package com.clearsolutions.task.dto;

import com.clearsolutions.task.validation.PatchValidation;
import com.clearsolutions.task.validation.ValidAge;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserRequest {

    @NotBlank(message = "email shouldn't be empty")
    @Email(message = "email is not valid")
    @Email(message = "email is not valid", groups = {PatchValidation.class})
    private String email;
    @NotBlank(message = "first name shouldn't be empty")
    private String firstName;
    @NotBlank(message = "last name shouldn't be empty")
    private String lastName;
    @NotNull(message = "birth date shouldn't be empty")
    @Past(message = "birth date should be in past")
    @Past(message = "birth date should be in past", groups = {PatchValidation.class})
    @ValidAge(message = "User must be at least {validAge} years old")
    private LocalDate birthDate;
    private String address;
    private String phoneNumber;
}
