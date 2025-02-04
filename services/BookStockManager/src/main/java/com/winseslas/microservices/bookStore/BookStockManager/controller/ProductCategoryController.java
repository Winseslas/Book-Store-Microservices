package com.winseslas.microservices.bookStore.BookStockManager.controller;

import com.winseslas.microservices.bookStore.BookStockManager.model.dto.ProductCategoryRequest;
import com.winseslas.microservices.bookStore.BookStockManager.model.dto.ProductCategoryResponse;
import com.winseslas.microservices.bookStore.BookStockManager.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/product-categories")
@Tag(name = "Product Categories", description = "Operations related to product categories")
public class ProductCategoryController {

    private static final Log LOG = LogFactory.getLog(ProductCategoryController.class);
    private final ProductCategoryService productCategoryService;

    @Autowired
    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    /**
     * Creates a new product category.
     *
     * @param productCategoryRequest the request body containing product category
     *                               details.
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product category", description = "Adds a new product category with the specified details.", tags = {
            "Product Categories"})
    public ResponseEntity<ProductCategoryResponse> createProductCategory(@RequestBody @Valid ProductCategoryRequest productCategoryRequest) {
        productCategoryService.createProductCategory(productCategoryRequest);
        return ResponseEntity.ok(productCategoryService.createProductCategory(productCategoryRequest));
    }

    /**
     * Retrieves all product categories.
     *
     * @return a list of all product categories
     */
    @GetMapping("/read-all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve all product categories", description = "Returns a list of all available product categories.", tags = {
            "Product Categories" })
    public ResponseEntity<List<ProductCategoryResponse>> getAllProductCategories() {
        LOG.info("Received request to retrieve all product categories");
        List<ProductCategoryResponse> productCategories = productCategoryService.getAllProductCategories();
        if (productCategories.isEmpty()) {
            LOG.info("No product categories found");
            return ResponseEntity.noContent().build();
        } else {
            LOG.info("Retrieved " + productCategories.size() + " product categories");
            return ResponseEntity.ok(productCategories);
        }
    }

    /**
     * Retrieves a product category by its ID.
     *
     * @param id the ID of the product category to retrieve.
     * @return the product category if found, or a 404 response if not found.
     */
    @GetMapping("/read-one/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve a product category by ID", description = "Fetches a specific product category using its unique ID.", tags = {
            "Product Categories" })
    public ResponseEntity<ProductCategoryResponse> getProductCategoryById(@PathVariable Long id) {
        LOG.info("Received request to retrieve product category with ID: " + id);
        Optional<ProductCategoryResponse> category = productCategoryService.getProductCategoryById(id);

        return category.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    LOG.warn("Product category with ID " + id + " not found.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }

    /**
     * Retrieves product categories based on a specific field and its value.
     *
     * @param fieldName The name of the field to filter by (e.g., "name",
     *                  "description").
     * @param value     The value to match for the specified field.
     * @return A ResponseEntity containing a list of matching ProductCategory
     *         objects.
     */
    @GetMapping("/search")
    @Operation(summary = "Search product categories by field", description = "Finds product categories based on a specific field (e.g., name or description) and its value.", tags = {
            "Product Categories" })
    public ResponseEntity<List<ProductCategoryResponse>> findProductCategoriesByField(@RequestParam String fieldName,
                                                                                      @RequestParam String value) {
        LOG.info("Searching for product categories with field '" + fieldName + "' and value '" + value + "'");

        // Retrieve the product categories based on the field and value
        List<ProductCategoryResponse> productCategories = productCategoryService.findProductCategoriesByField(fieldName,
                value);

        // Return 204 No Content if no results are found
        if (productCategories.isEmpty()) {
            LOG.info("No product categories found for field '" + fieldName + "' and value '" + value + "'");
            return ResponseEntity.noContent().build();
        }

        // Return 200 OK with the list of product categories
        LOG.info("Successfully retrieved " + productCategories.size() + " product categories for field '" + fieldName
                + "'");
        return ResponseEntity.ok(productCategories);
    }

    /**
     * Updates an existing product category.
     *
     * @param id                     the ID of the product category to update.
     * @param productCategoryRequest the request body containing updated product
     *                               category details.
     * @return a ResponseEntity indicating the result of the update operation.
     */
    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a product category", description = "Updates the details of an existing product category using its unique ID.", tags = {
            "Product Categories" })
    public ResponseEntity<String> updateProductCategory(
            @PathVariable Long id,
            @RequestBody ProductCategoryRequest productCategoryRequest) throws Exception {
        LOG.info("Received request to update product category with ID: " + id);
        boolean updated = productCategoryService.updateProductCategory(id, productCategoryRequest);
        if (updated) {
            LOG.info("Product category with ID " + id + " successfully updated.");
            return ResponseEntity.ok("Product category updated successfully.");
        } else {
            LOG.warn("Failed to update product category with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product category not found.");
        }
    }

    /**
     * Deletes a product category by its ID.
     *
     * @param id the ID of the product category to delete.
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product category by ID", description = "Removes a product category from the system using its unique ID.", tags = {
            "Product Categories" })
    public ResponseEntity<Void> deleteProductCategory(@PathVariable Long id) {
        LOG.info("Received request to delete product category with ID: " + id);
        try {
            productCategoryService.deleteProductCategory(id);
            LOG.info("Product category with ID " + id + " successfully deleted.");
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            LOG.warn("Failed to delete product category. Reason: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
