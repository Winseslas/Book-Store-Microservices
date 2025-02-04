package com.winseslas.microservices.bookStore.UserManager.controller;

import com.winseslas.microservices.bookStore.UserManager.model.dto.BPartnerGroupRequest;
import com.winseslas.microservices.bookStore.UserManager.model.dto.BPartnerGroupResponse;
import com.winseslas.microservices.bookStore.UserManager.service.BPartnerGroupService;
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

/**
 * REST controller for managing business partner groups.
 */
@RestController
@RequestMapping("/api/v1/business-partners-group")
@Tag(name = "Business Partners Group", description = "Operations related to managing business partner groups.")
public class BPartnerGroupController {

    private static final Log LOG = LogFactory.getLog(BPartnerGroupController.class);
    private final BPartnerGroupService bpartnerGroupService;

    @Autowired
    public BPartnerGroupController(BPartnerGroupService bpartnerGroupService) {
        this.bpartnerGroupService = bpartnerGroupService;
    }

    /**
     * Creates a new business partner group.
     *
     * @param request the request containing the details of the new group.
     * @return the created group details.
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new business partner group",
            description = "Creates a new business partner group with the provided details.",
            tags = {"Business Partners Group"})
    public ResponseEntity<BPartnerGroupResponse> createBPartnerGroup(
            @RequestBody @Valid BPartnerGroupRequest request) {
        LOG.info("Received request to create a new business partner group.");
        BPartnerGroupResponse response = bpartnerGroupService.createBPartnerGroup(request);
        LOG.info("Successfully created a new business partner group.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all business partner groups.
     *
     * @return a list of all existing groups.
     */
    @GetMapping("/read-all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve all business partner groups",
            description = "Fetches a list of all available business partner groups.",
            tags = {"Business Partners Group"})
    public ResponseEntity<List<BPartnerGroupResponse>> getAllBusinessPartnerGroups() {
        LOG.info("Fetching all business partner groups.");
        List<BPartnerGroupResponse> groups = bpartnerGroupService.getAllBusinessPartnersGroup();

        if (groups.isEmpty()) {
            LOG.info("No business partner groups found.");
            return ResponseEntity.notFound().build();
        }

        LOG.info("Successfully retrieved " + groups.size() + " business partner groups.");
        return ResponseEntity.ok(groups);
    }

    /**
     * Retrieves a business partner group by its ID.
     *
     * @param id the ID of the group to retrieve.
     * @return the group details if found.
     */
    @GetMapping("/read-one/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve a business partner group by ID",
            description = "Fetches a specific business partner group using its unique ID.",
            tags = {"Business Partners Group"})
    public ResponseEntity<BPartnerGroupResponse> getBusinessPartnerGroupById(@PathVariable Long id) {
        LOG.info("Fetching business partner group with ID: " + id);
        Optional<BPartnerGroupResponse> group = bpartnerGroupService.getBPartnerGroupById(id);

        if (group.isPresent()) {
            LOG.info("Successfully retrieved business partner group with ID: " + id);
            return ResponseEntity.ok(group.get());
        }

        LOG.warn("Business partner group with ID " + id + " not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Updates an existing business partner group.
     *
     * @param id      the ID of the group to update.
     * @param request the request containing updated group details.
     * @return a response indicating the outcome of the update operation.
     */
    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a business partner group",
            description = "Updates the details of an existing business partner group.",
            tags = {"Business Partners Group"})
    public ResponseEntity<String> updateBusinessPartnerGroup(
            @PathVariable Long id,
            @RequestBody @Valid BPartnerGroupRequest request) throws Exception {
        LOG.info("Updating business partner group with ID: " + id);

        boolean isUpdated = bpartnerGroupService.updateBPartnerGroup(id, request);

        if (isUpdated) {
            LOG.info("Successfully updated business partner group with ID: " + id);
            return ResponseEntity.ok("Business partner group updated successfully.");
        }

        LOG.warn("Failed to update business partner group with ID: " + id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Business partner group not found.");
    }

    /**
     * Deletes a business partner group by its ID.
     *
     * @param id the ID of the group to delete.
     */
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a business partner group by ID",
            description = "Removes a business partner group using its unique ID.",
            tags = {"Business Partners Group"})
    public ResponseEntity<Void> deleteBusinessPartnerGroup(@PathVariable Long id) {
        LOG.info("Deleting business partner group with ID: " + id);

        try {
            bpartnerGroupService.deleteBPartnerGroup(id);
            LOG.info("Successfully deleted business partner group with ID: " + id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            LOG.error("Failed to delete business partner group with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
