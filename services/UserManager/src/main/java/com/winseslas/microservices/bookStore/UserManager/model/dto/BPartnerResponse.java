package com.winseslas.microservices.bookStore.UserManager.model.dto;

import com.winseslas.microservices.bookStore.UserManager.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Builder
@Setter
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class BPartnerResponse {
    private Long id;
    private String value;
    private String name;
    private String description;
    private boolean isActive;
    private Boolean isCustomer;
    private Boolean isAuthor;
    private Boolean isEmployee;
    private String profileUrl;
    private Gender gender;
    private BPartnerGroupResponse bPartnerGroup;
}
