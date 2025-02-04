package com.winseslas.microservices.bookStore.UserManager.service;

import com.winseslas.microservices.bookStore.UserManager.model.dto.UserResponse;
import com.winseslas.microservices.bookStore.UserManager.model.dto.UserRequest;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.Role;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.User;
import com.winseslas.microservices.bookStore.UserManager.repository.UserRepository;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing user-related operations.
 * This class provides methods for creating, retrieving, updating, and deleting users.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private static final Log LOG = LogFactory.getLog(UserService.class);

    /**
     * Constructor to initialize the UserService with the UserRepository dependency.
     *
     * @param userRepository the repository for accessing user data.
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user based on the provided UserRequest.
     *
     * @param userRequest the user details for creating a new user.
     * @return a UserResponse containing the created user's details.
     */
    @Transactional
    public UserResponse createUser(@Valid UserRequest userRequest) {
        User newUser = User.builder()
                .value(userRequest.getValue())
                .name(userRequest.getName())
                .description(userRequest.getDescription())
                .isActive(true)
                .email(userRequest.getEmail())
                .password("password")
                .phoneNumber(userRequest.getPhoneNumber())
                .dateOfBirth(userRequest.getDateOfBirth())
                .build();
        userRepository.save(newUser);
        LOG.info("User created successfully with ID: " + newUser.getId());
        return mapToUserResponse(newUser);
    }

    /**
     * Retrieves all users from the database.
     *
     * @return a list of UserResponse objects representing all users, or an empty list if no users are found.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        LOG.info("Retrieving all users.");
        List<User> allUser = userRepository.findAll();

        if (allUser.isEmpty()) {
            LOG.warn("No user found.");
            return List.of();
        }

        LOG.info("Found " + allUser.size() + " user(s).");
        return allUser.stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id the ID of the user to retrieve.
     * @return an Optional containing the UserResponse if found, or empty if not found.
     */
    @Transactional
    public Optional<UserResponse> getUserById(Long id) {
        LOG.info("Retrieving user with ID: " + id);
        Optional<User> optionalUser = this.userRepository.findById(Math.toIntExact(id));

        if (optionalUser.isPresent()) {
            LOG.info("User found with ID: " + id);
            return Optional.of(mapToUserResponse(optionalUser.get()));
        } else {
            LOG.warn("No user found with ID: " + id);
            return Optional.empty();
        }
    }

    /**
     * Checks if a user with the given email already exists in the database.
     *
     * @param email the email address to search for.
     * @return true if a user with the given email exists, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        LOG.info("Checking if user exists with email: " + email);
        boolean exists = this.userRepository.findByEmail(email).isPresent();
        LOG.info("User existence check result: " + exists);
        return exists;
    }

    /**
     * Updates an existing user's information.
     *
     * @param id          the ID of the user to update.
     * @param userRequest the new user details.
     * @return true if the update was successful, false otherwise.
     * @throws Exception if the user with the given ID is not found.
     */
    @Transactional
    public boolean updateUser(Long id, @Valid UserRequest userRequest) throws Exception {
        Optional<User> optionalUser = this.userRepository.findById(Math.toIntExact(id));
        if (optionalUser.isEmpty()) {
            LOG.warn("No user found with ID: " + id);
            throw new Exception("User not found with id: " + id);
        }

        User existingUser = optionalUser.get();
        if (userRequest.getValue() != null) {
            existingUser.setValue(userRequest.getValue());
        }

        if (userRequest.getName() != null) {
            existingUser.setName(userRequest.getName());
        }

        if (userRequest.getDescription() != null) {
            existingUser.setDescription(userRequest.getDescription());
        }

        // Save the updated user
        userRepository.save(existingUser);
        LOG.info("User updated successfully with ID: " + id);
        return true;
    }

    /**
     * Deletes a user by their unique ID.
     *
     * @param id the ID of the user to delete.
     * @throws Exception if the user with the given ID is not found.
     */
    @Transactional
    public void deleteUser(Long id) throws Exception {
        LOG.info("Deleting user with ID: " + id);
        Optional<User> optionalUser = userRepository.findById(Math.toIntExact(id));
        if (optionalUser.isEmpty()) {
            LOG.warn("No user found with ID: " + id);
            throw new Exception("User not found with id: " + id);
        }
        userRepository.deleteById(Math.toIntExact(id));
        LOG.info("User deleted successfully with ID: " + id);
    }

    /**
     * Maps a User entity to a UserResponse DTO.
     *
     * @param user the User entity to map.
     * @return a UserResponse object containing the user's details.
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .value(user.getValue())
                .name(user.getName())
                .description(user.getDescription())
                .isActive(user.isActive())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .failedLoginCount(user.getFailedLoginCount())
                .dateLastLogin(user.getDateLastLogin())
                .datePasswordChanged(user.getDatePasswordChanged())
                .dateOfBirth(user.getDateOfBirth())
                .bpartner(user.getBpartner() != null ? user.getBpartner().getName() : null)
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .toList())
                .build();
    }
}
