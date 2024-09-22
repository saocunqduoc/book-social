package com.nguyenvanlinh.identityservice.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.nguyenvanlinh.identityservice.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

// Lombok
// ----------
@Getter
@Setter
@Data
// No constructor
@NoArgsConstructor
// All attribute constructor
@AllArgsConstructor
@Builder
// modify chung -> không cần khai báo field
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 6, message = "USERNAME_INVALID") // tên của Error Code
    String username;

    @Size(min = 10, message = "PASSWORD_INVALID")
    String password;

    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "EMAIL_IS_REQUIRED")
    String email;

    String firstName;
    String lastName;

    @DobConstraint(min = 15, message = "INVALID_DOB")
    LocalDate dob;

    String city;
}
