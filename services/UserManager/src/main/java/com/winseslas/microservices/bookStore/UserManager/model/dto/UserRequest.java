package com.winseslas.microservices.bookStore.UserManager.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @NotNull(message = "The 'name' field must not be null.")
    private String name;
    @NotNull(message = "The 'value' field must not be null.")
    private String value;
    @NotNull(message = "The 'email' field must not be null.")
    private String email;
    @NotNull(message = "The 'isActive' field must not be null.")
    private boolean isActive;

    private String description;
    private String phoneNumber;
    private Timestamp dateOfBirth;
    private Instant dateLastLogin;
    private Integer failedLoginCount;
    private Timestamp datePasswordChanged;
}
