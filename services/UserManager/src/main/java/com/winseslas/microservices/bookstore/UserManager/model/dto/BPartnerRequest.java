package com.winseslas.microservices.bookstore.UserManager.model.dto;

import com.winseslas.microservices.bookstore.UserManager.model.enums.Gender;
import com.winseslas.microservices.bookstore.UserManager.validation.ValidEmail;
import com.winseslas.microservices.bookstore.UserManager.validation.ValidPhone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BPartnerRequest {
    @NotBlank(message = "La valeur est obligatoire")
    private String value;

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    private String description;

    @ValidEmail
    private String email;

    @ValidPhone
    private String phone;

    @NotNull(message = "Le statut client est obligatoire")
    private Boolean isCustomer;

    @NotNull(message = "Le statut auteur est obligatoire")
    private Boolean isAuthor;

    @NotNull(message = "Le statut employé est obligatoire")
    private Boolean isEmployee;

    @Builder.Default
    private String profileUrl = "https://www.svgrepo.com/show/452030/avatar-default.svg";

    @NotNull(message = "Le genre est obligatoire")
    private Gender gender;

    @NotNull(message = "Le groupe est obligatoire")
    private Long bPartnerGroupId;
}
