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

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Setter
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
    @NotNull
    private Long id;
    @NotNull
    private String value;
    @NotNull
    private String name;
    private String description;
    @NotNull
    private boolean isActive;
    private List<UserResponse> userResponses = new ArrayList<>();
}
