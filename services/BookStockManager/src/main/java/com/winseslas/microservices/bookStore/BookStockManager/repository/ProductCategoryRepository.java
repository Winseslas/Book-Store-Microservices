package com.winseslas.microservices.bookStore.BookStockManager.repository;

import com.winseslas.microservices.bookStore.BookStockManager.model.entitie.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {
}