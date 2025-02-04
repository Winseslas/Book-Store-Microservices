package com.winseslas.microservices.bookStore.BookStockManager.model.dto;


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
public class ProductCategoryRequest {
    @NotNull(message = "The 'value' field must not be null.")
    private String value;
    @NotNull(message = "The 'name' field must not be null.")
    private String name;
    private String description;
}