package com.winseslas.microservices.bookStore.UserManager.controller;

import com.winseslas.microservices.bookStore.UserManager.model.dto.RoleRequest;
import com.winseslas.microservices.bookStore.UserManager.model.dto.RoleResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.ErrorResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.NoContentResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.SuccessResponse;
import com.winseslas.microservices.bookStore.UserManager.model.response.UnauthorizedResponse;
import com.winseslas.microservices.bookStore.UserManager.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/role")
@Tag(name = "Role", description = "Operations related to role")
public class RoleController {

    private static final Log LOG = LogFactory.getLog(RoleController.class);
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new role", description = "Creates a new role with the provided details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Role successfully created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request: Invalid role details provided", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User is not authenticated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: A role with the given value and name already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> createRole(@RequestBody @Valid RoleRequest roleRequest) {
        LOG.info("Received request to create a new role with value: " + roleRequest.getValue() + " and name: " + roleRequest.getName());
        Map<String, Object> response = new HashMap<>();

        if (roleService.existsByValueAndName(roleRequest.getValue(), roleRequest.getName())) {
            LOG.warn("A role with the given value: " + roleRequest.getValue() + " and name: " + roleRequest.getName() + " already exists.");
            response.put("success", false);
            response.put("error", "A role with the given value and name already exists.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        RoleResponse createdRole = roleService.createRole(roleRequest);
        LOG.info("Role successfully created with ID: " + createdRole.getId());
        response.put("success", true);
        response.put("data", createdRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/read-all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve all roles", description = "Returns a list of all available roles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "204", description = "No Content: There are no roles available", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoContentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User is not authenticated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> getAllRoles() {
        LOG.info("Received request to retrieve all roles");
        Map<String, Object> response = new HashMap<>();
        List<RoleResponse> roles = roleService.getAllRoles();
        if (roles.isEmpty()) {
            LOG.info("No roles found");
            response.put("success", false);
            response.put("message", "No Content. No data");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        }

        LOG.info("Retrieved " + roles.size() + " roles");
        response.put("success", true);
        response.put("data", roles);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/read-one/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve a role by ID", description = "Fetches a specific role using its unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: Role with the given ID does not exist", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User is not authenticated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        LOG.info("Received request to retrieve role with ID: " + id);
        Optional<RoleResponse> role = roleService.getRoleById(id);
        return role.map(ResponseEntity::ok).orElseGet(() -> {
            LOG.warn("Role with ID " + id + " not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a role", description = "Updates the details of an existing role using its unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: Role with the given ID does not exist", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User is not authenticated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> updateRole(@PathVariable Long id, @RequestBody @Valid RoleRequest roleRequest) throws Exception {
        LOG.info("Received request to update role with ID: " + id);
        Map<String, Object> response = new HashMap<>();
        boolean updated = roleService.updateRole(id, roleRequest);
        if (updated) {
            LOG.info("Role with ID " + id + " successfully updated.");
            response.put("success", true);
            response.put("data", "successfully updated.");
            return ResponseEntity.ok(response);
        } else {
            LOG.warn("Failed to update role with ID: " + id);
            response.put("success", false);
            response.put("data", "Failed to update role.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a role by ID", description = "Removes a role from the system using its unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Role successfully deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found: Role with the given ID does not exist", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User is not authenticated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        LOG.info("Received request to delete role with ID: " + id);
        try {
            roleService.deleteRole(id);
            LOG.info("Role with ID " + id + " successfully deleted.");
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            LOG.warn("Failed to delete role. Reason: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
