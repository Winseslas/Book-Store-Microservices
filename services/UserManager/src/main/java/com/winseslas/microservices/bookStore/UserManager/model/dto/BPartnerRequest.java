package com.winseslas.microservices.bookStore.UserManager.model.dto;

import com.winseslas.microservices.bookStore.UserManager.model.enums.Gender;
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
public class BPartnerRequest {
    @NotNull(message = "The 'value' field must not be null.")
    private String value;
    @NotNull(message = "The 'name' field must not be null.")
    private String name;
    private String description;
    @NotNull(message = "The 'isCustomer' field must not be null.")
    private Boolean isCustomer;
    @NotNull(message = "The 'isAuthor' field must not be null.")
    private Boolean isAuthor;
    @NotNull(message = "The 'isEmployee' field must not be null.")
    private Boolean isEmployee;
    private String profileUrl = "https://www.svgrepo.com/show/452030/avatar-default.svg";
    @NotNull
    private Gender gender;
    @NotNull(message = "The 'bPartnerGroupId' field must not be null.")
    private Long bPartnerGroupId;
}
