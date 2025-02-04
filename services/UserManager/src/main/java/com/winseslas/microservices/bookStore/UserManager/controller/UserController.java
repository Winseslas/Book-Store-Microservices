package com.winseslas.microservices.bookStore.UserManager.controller;

import com.winseslas.microservices.bookStore.UserManager.model.dto.UserRequest;
import com.winseslas.microservices.bookStore.UserManager.model.dto.UserResponse;
import com.winseslas.microservices.bookStore.UserManager.service.UserService;
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
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Operations related to user management")
public class UserController {

    private static final Log LOG = LogFactory.getLog(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details.", tags = {"Users"})
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {
        LOG.info("Received request to create a new user");
        UserResponse userResponse = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @GetMapping("/read-all")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve all users", description = "Returns a list of all registered users.", tags = {"Users"})
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        LOG.info("Received request to retrieve all users");
        List<UserResponse> users = userService.getAllUsers();
        if (users.isEmpty()) {
            LOG.info("No users found");
            return ResponseEntity.noContent().build();
        }
        LOG.info("Retrieved " + users.size() + " users");
        return ResponseEntity.ok(users);
    }

    @GetMapping("/read-one/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve a user by ID", description = "Fetches a specific user using their unique ID.", tags = {"Users"})
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        LOG.info("Received request to retrieve user with ID: " + id);
        Optional<UserResponse> userResponse = userService.getUserById(id);
        return userResponse.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    LOG.warn("User with ID " + id + " not found.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a user", description = "Updates the details of an existing user by their unique ID.", tags = {"Users"})
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserRequest userRequest) throws Exception {
        LOG.info("Received request to update user with ID: " + id);
        boolean updated = userService.updateUser(id, userRequest);
        if (updated) {
            LOG.info("User with ID " + id + " successfully updated.");
            return ResponseEntity.ok("User updated successfully.");
        } else {
            LOG.warn("Failed to update user with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user by ID", description = "Removes a user from the system using their unique ID.", tags = {"Users"})
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        LOG.info("Received request to delete user with ID: " + id);
        try {
            userService.deleteUser(id);
            LOG.info("User with ID " + id + " successfully deleted.");
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            LOG.warn("Failed to delete user. Reason: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
