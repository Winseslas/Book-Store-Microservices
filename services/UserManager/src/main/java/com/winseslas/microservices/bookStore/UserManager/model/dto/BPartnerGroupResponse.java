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

@Builder
@Data
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BPartnerGroupResponse {
    @NotNull
    private Long id;
    private String value;
    private String name;
    private String description;
    private boolean isActive;
    private Boolean isCustomer;
    private Boolean isAuthor;
    private Boolean isEmployee;
    private List<BPartnerResponse> bpartners = new ArrayList<>();
}
