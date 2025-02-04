package com.winseslas.microservices.bookStore.UserManager.model.response;

import com.winseslas.microservices.bookStore.UserManager.validation.FieldMatch;
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
        first = "newPassword",
        second = "confirmPassword",
        message = "Password and confirmation must be identical"
)
public class PasswordResetRequest {
    @NotBlank(message = "New password is mandatory")
    @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is mandatory")
    private String confirmPassword;
}