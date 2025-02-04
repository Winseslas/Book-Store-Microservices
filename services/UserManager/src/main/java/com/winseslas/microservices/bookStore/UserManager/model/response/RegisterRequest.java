package com.winseslas.microservices.bookStore.UserManager.model.response;

import com.winseslas.microservices.bookStore.UserManager.validation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldMatch(
        first = "password",
        second = "confirmPassword",
        message = "Password and confirmation must be identical"
)
public class RegisterRequest {
    @NotBlank(message = "Name is mandatory")
    @Size(min = 2, max = 50, message = "Name must be between 2-50 characters")
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
    private String password;

    @NotBlank(message = "Password confirmation is mandatory")
    private String confirmPassword;
}