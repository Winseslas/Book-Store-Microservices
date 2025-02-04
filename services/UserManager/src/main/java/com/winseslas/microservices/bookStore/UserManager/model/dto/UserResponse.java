package com.winseslas.microservices.bookStore.UserManager.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@Setter
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    @NotNull
    private Long id;
    @NotNull
    private String value;
    @NotNull
    private String name;
    @NotNull
    private String email;
    @NotNull
    private boolean isActive;

    private String description;
    private String phoneNumber;
    private Timestamp dateOfBirth;
    private Instant dateLastLogin;
    private Integer failedLoginCount;
    private Timestamp datePasswordChanged;
    private List<String> roles;
    private String bpartner;
}
