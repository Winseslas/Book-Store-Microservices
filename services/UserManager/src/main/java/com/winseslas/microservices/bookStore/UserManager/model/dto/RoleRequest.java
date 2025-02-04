package com.winseslas.microservices.bookStore.UserManager.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    @NotNull(message = "The 'value' field must not be null.")
    private String value;
    @NotNull(message = "The 'name' field must not be null.")
    private String name;
    private String description;
    @NotNull
    private boolean isActive;
}
