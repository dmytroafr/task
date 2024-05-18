package com.clearsolutions.task.dto;

import com.clearsolutions.task.validation.PatchValidation;
import com.clearsolutions.task.validation.PutValidation;
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

    @NotBlank(message = "Email shouldn't be empty", groups = {PutValidation.class})
    @Email(message = "Email is not valid", groups = {PutValidation.class, PatchValidation.class})
    private String email;

    @NotBlank(message = "First name shouldn't be empty", groups = {PutValidation.class})
    private String firstName;

    @NotBlank(message = "Last name shouldn't be empty", groups = {PutValidation.class})
    private String lastName;

    @NotNull(message = "Birth date shouldn't be empty", groups = {PutValidation.class})
    @Past(message = "Birth date should be in past", groups = {PutValidation.class, PatchValidation.class})
    @ValidAge(groups = {PutValidation.class, PatchValidation.class})
    private LocalDate birthDate;

    private String address;
    private String phoneNumber;
}
