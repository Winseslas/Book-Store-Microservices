package com.winseslas.microservices.bookStore.UserManager.controller;

import com.winseslas.microservices.bookStore.UserManager.model.dto.BPartnerRequest;
import com.winseslas.microservices.bookStore.UserManager.model.dto.BPartnerResponse;
import com.winseslas.microservices.bookStore.UserManager.service.BPartnerGroupService;
import com.winseslas.microservices.bookStore.UserManager.service.BPartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/business-partners")
@Tag(name = "Business Partners", description = "Operations related to business partners")
public class BPartnerController {
    private static final Log LOG = LogFactory.getLog(BPartnerController.class);
    private final BPartnerService bpartnerService;
    private final BPartnerGroupService bpartnerGroupService;

    @Autowired
    public BPartnerController(BPartnerService bpartnerService, BPartnerGroupService bpartnerGroupService) {
        this.bpartnerService = bpartnerService;
        this.bpartnerGroupService = bpartnerGroupService;
    }

    /**
     * Creates a new business partner.
     *
     * @param businessPartnerRequest the request containing the details of the business partner to be created
     * @return the created business partner as a response
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new business partner", description = "Creates a new business partner with the provided details.", tags = {
            "Business Partners" })
    public ResponseEntity<BPartnerResponse> createBusinessPartner(
            @RequestBody @Valid BPartnerRequest businessPartnerRequest) {
        LOG.info("Received request to create a new business partner");

        bpartnerGroupService.getBPartnerGroupById(businessPartnerRequest.getBPartnerGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Business Partner Group not found"));

        BPartnerResponse createdBusinessPartner = bpartnerService.createBPartner(businessPartnerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBusinessPartner);
    }


    /**
     * Retrieves all business partners.
     *
     * @return a list of all business partners
     */
    @GetMapping("/read-all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve all business partners", description = "Returns a list of all available business partners.", tags = {
            "Business Partners" })
    public ResponseEntity<List<BPartnerResponse>> getAllBusinessPartners() {
        LOG.info("Received request to retrieve all business partners");
        List<BPartnerResponse> businessPartners = bpartnerService.getAllBusinessPartners();
        if (businessPartners.isEmpty()) {
            LOG.info("No business partners found");
            return ResponseEntity.notFound().build();
        } else {
            LOG.info("Retrieved " + businessPartners.size() + " business partners");
            return ResponseEntity.ok(businessPartners);
        }
    }

    /**
     * Retrieves a business partners by its ID.
     *
     * @param id the ID of the business partners to retrieve.
     * @return the business partners if found, or a 404 response if not found.
     */
    @GetMapping("/read-one/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve a business partners by ID", description = "Fetches a specific business partners using its unique ID.", tags = {
            "Business Partners" })
    public ResponseEntity<BPartnerResponse> getBPartnerById(@PathVariable Long id) {
        LOG.info("Received request to retrieve business partners with ID: " + id);
        Optional<BPartnerResponse> businessPartners = bpartnerService.getBPartnerById(id);

        return businessPartners.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    LOG.warn("Business partner with ID " + id + " not found.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }

    /**
     * Retrieves business partners based on a specific field and its value.
     *
     * @param fieldName The name of the field to filter by (e.g., "name",
     *                  "description").
     * @param value     The value to match for the specified field.
     * @return A ResponseEntity containing a list of matching BPartner
     *         objects.
     */
    @GetMapping("/search")
    @Operation(summary = "Search business partners by field", description = "Finds business partners based on a specific field (e.g., name or description) and its value.", tags = {
            "Business Partners" })
    public ResponseEntity<List<BPartnerResponse>> findBusinessPartnersByField(@RequestParam String fieldName,
                                                                                      @RequestParam String value) {
        LOG.info("Searching for business partners with field '" + fieldName + "' and value '" + value + "'");

        // Retrieve the business partners based on the field and value
        List<BPartnerResponse> businessPartners = bpartnerService.findBusinessPartnersByField(fieldName,
                value);

        // Return 204 No Content if no results are found
        if (businessPartners.isEmpty()) {
            LOG.info("No business partners found for field '" + fieldName + "' and value '" + value + "'");
            return ResponseEntity.noContent().build();
        }

        // Return 200 OK with the list of business partners
        LOG.info("Successfully retrieved " + businessPartners.size() + " business partners for field '" + fieldName
                + "'");
        return ResponseEntity.ok(businessPartners);
    }

    /**
     * Updates an existing business partners.
     *
     * @param id                     the ID of the business partners to update.
     * @param BusinessPartnersRequest the request body containing updated product
     *                               businessPartners details.
     * @return a ResponseEntity indicating the result of the update operation.
     */
    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a business partners", description = "Updates the details of an existing business partners using its unique ID.", tags = {
            "Business Partners" })
    public ResponseEntity<String> updateBPartner(
            @PathVariable Long id,
            @RequestBody BPartnerRequest BusinessPartnersRequest) throws Exception {
        LOG.info("Received request to update business partners with ID: " + id);
        boolean updated = bpartnerService.updateBPartner(id, BusinessPartnersRequest);
        if (updated) {
            LOG.info("business partner with ID " + id + " successfully updated.");
            return ResponseEntity.ok("Business partner updated successfully.");
        } else {
            LOG.warn("Failed to update business partners with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("business partner not found.");
        }
    }

    /**
     * Deletes a business partners by its ID.
     *
     * @param id the ID of the business partners to delete.
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a business partners by ID", description = "Removes a business partners from the system using its unique ID.", tags = {
            "Business Partners" })
    public ResponseEntity<Void> deleteBPartner(@PathVariable Long id) {
        LOG.info("Received request to delete business partners with ID: " + id);
        try {
            bpartnerService.deleteBPartner(id);
            LOG.info("Business partner with ID " + id + " successfully deleted.");
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            LOG.warn("Failed to delete business partners. Reason: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
