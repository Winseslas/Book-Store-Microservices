package com.winseslas.microservices.bookStore.BookStockManager.service;

import com.winseslas.microservices.bookStore.BookStockManager.model.dto.ProductCategoryRequest;
import com.winseslas.microservices.bookStore.BookStockManager.model.dto.ProductCategoryResponse;
import com.winseslas.microservices.bookStore.BookStockManager.model.entitie.ProductCategory;
import com.winseslas.microservices.bookStore.BookStockManager.repository.ProductCategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductCategoryService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ProductCategoryRepository productCategoryRepository;
    private static final Log LOG = LogFactory.getLog(ProductCategoryService.class);

    public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    /**
     * Creates a new product category.
     *
     * @param productCategoryRequest the request body containing the new product category details.
     * @return a {@link ProductCategoryResponse} containing the created product category details.
     */
    @Transactional
    public ProductCategoryResponse createProductCategory(ProductCategoryRequest productCategoryRequest) {
        ProductCategory newCategory = ProductCategory.builder()
                .value(productCategoryRequest.getValue())
                .name(productCategoryRequest.getName())
                .description(productCategoryRequest.getDescription())
                .isActive(true)
                .build();
        productCategoryRepository.save(newCategory);
        LOG.info("Product category created successfully with ID: " + newCategory.getId());
        return mapToProductCategoryResponse(newCategory);
    }

    /**
     * Retrieves all product categories from the repository.
     *
     * @return a list of ProductCategoryResponse objects representing all
     *         available product categories. Returns an empty list if no
     *         categories are found.
     */
    @Transactional
    public List<ProductCategoryResponse> getAllProductCategories() {
        LOG.info("Retrieving all product categories.");
        List<ProductCategory> allProductCategory = productCategoryRepository.findAll();

        if (allProductCategory.isEmpty()) {
            LOG.warn("No product categories found.");
            return List.of();
        }

        LOG.info("Found " + allProductCategory.size() + " product category(ies).");
        return allProductCategory.stream()
                .map(this::mapToProductCategoryResponse)
                .toList();
    }

    /**
     * Retrieves a product category by its ID.
     *
     * @param id the ID of the product category to retrieve.
     * @return an Optional containing the product category if found, or an empty
     *         Optional if not found.
     */
    @Transactional
    public Optional<ProductCategoryResponse> getProductCategoryById(Long id) {
        LOG.info("Retrieving product category with ID: " + id);
        Optional<ProductCategory> optionalCategory = productCategoryRepository.findById(Math.toIntExact(id));

        if (optionalCategory.isPresent()) {
            LOG.info("Product category found with ID: " + id);
            return Optional.of(mapToProductCategoryResponse(optionalCategory.get()));
        } else {
            LOG.warn("No product category found with ID: " + id);
            return Optional.empty();
        }
    }

    /**
     * Finds product categories by a specific field name and value.
     *
     * @param fieldName the name of the field to search by
     * @param value     the value to match
     * @return a list of product categories matching the criteria
     */
    @Transactional
    public List<ProductCategoryResponse> findProductCategoriesByField(String fieldName, Object value) {
        LOG.info("Searching for product categories by field: " + fieldName + ", value: " + value);

        // Construct a dynamic JPQL query
        String queryStr = "SELECT pc FROM ProductCategory pc WHERE pc." + fieldName + " = :value";
        TypedQuery<ProductCategory> query = entityManager.createQuery(queryStr, ProductCategory.class);
        query.setParameter("value", value);

        // Execute the query and retrieve the result list
        List<ProductCategory> categories = query.getResultList();
        if (categories.isEmpty()) {
            LOG.warn("No product categories found.");
            return List.of();
        }

        LOG.info("Found " + categories.size() + " product category(ies) for field: " + fieldName);
        return categories.stream()
                .map(this::mapToProductCategoryResponse)
                .toList();
    }

    @Transactional
    public boolean updateProductCategory(Long id, ProductCategoryRequest productCategoryRequest) throws Exception {
        Optional<ProductCategory> optionalCategory = this.productCategoryRepository.findById(Math.toIntExact(id));
        if (optionalCategory.isEmpty()) {
            LOG.warn("No product category found with ID: " + id);
            throw new Exception("Category not found with id: " + id);
        }

        ProductCategory existingCategory = optionalCategory.get();
        existingCategory.setValue(productCategoryRequest.getValue());
        existingCategory.setName(productCategoryRequest.getName());

        if (productCategoryRequest.getDescription() != null) {
            existingCategory.setDescription(productCategoryRequest.getDescription());
        }
        // Save the updated product category
        productCategoryRepository.save(existingCategory);
        LOG.info("Product category updated successfully with ID: " + id);
        return true;
    }

    /**
     * Deletes a product category by its ID.
     *
     * @param id the ID of the product category to delete.
     * @throws Exception if the product category is not found.
     */
    @Transactional
    public void deleteProductCategory(Long id) throws Exception {
        LOG.info("Deleting product category with ID: " + id);
        Optional<ProductCategory> optionalCategory = productCategoryRepository.findById(Math.toIntExact(id));
        if (optionalCategory.isEmpty()) {
            LOG.warn("No product category found with ID: " + id);
            throw new Exception("Category not found with id: " + id);
        }
        productCategoryRepository.deleteById(Math.toIntExact(id));
        LOG.info("Product category deleted successfully with ID: " + id);
    }

    /**
     * Maps a ProductCategory object to a ProductCategoryResponse object.
     *
     * @param productCategory the ProductCategory object to map.
     * @return a ProductCategoryResponse object.
     */
    private ProductCategoryResponse mapToProductCategoryResponse(ProductCategory productCategory) {
        return ProductCategoryResponse.builder()
                .id(productCategory.getId())
                .value(productCategory.getValue())
                .name(productCategory.getName())
                .description(productCategory.getDescription())
                .isActive(productCategory.isActive())
                .build();
    }
}
