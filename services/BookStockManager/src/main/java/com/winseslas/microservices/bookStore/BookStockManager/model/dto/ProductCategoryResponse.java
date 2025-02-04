package com.winseslas.microservices.bookStore.BookStockManager.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@Setter
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ProductCategoryResponse {
    @NotNull
    private Long id;
    @NotNull
    private String value;
    @NotNull
    private String name;
    private String description;
    @NotNull
    private boolean isActive;
}