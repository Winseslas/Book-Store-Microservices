package com.winseslas.microservices.bookStore.UserManager.service;

import com.winseslas.microservices.bookStore.UserManager.model.dto.RoleRequest;
import com.winseslas.microservices.bookStore.UserManager.model.dto.RoleResponse;
import com.winseslas.microservices.bookStore.UserManager.model.dto.UserResponse;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.Role;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.User;
import com.winseslas.microservices.bookStore.UserManager.repository.RoleRepository;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private static final Log LOG = LogFactory.getLog(RoleService.class);

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Creates a new role in the system.
     *
     * @param roleRequest the data required to create a new role, including its value, name, and description.
     * @return a {@link RoleResponse} object representing the newly created role.
     * @throws jakarta.validation.ConstraintViolationException if the provided role data is invalid.
     */
    @Transactional
    public RoleResponse createRole(@Valid RoleRequest roleRequest) {
        Role newRole = Role.builder()
                .value(roleRequest.getValue())
                .name(roleRequest.getName())
                .description(roleRequest.getDescription())
                .isActive(true)
                .build();
        roleRepository.save(newRole);
        LOG.info("Role created successfully with ID: " + newRole.getId());
        return mapToRoleResponse(newRole);
    }

    /**
     * Retrieves a list of all roles in the system.
     *
     * @return a list of {@link RoleResponse} objects representing all roles.
     *         Returns an empty list if no roles are found.
     */
    @Transactional
    public List<RoleResponse> getAllRoles() {
        LOG.info("Retrieving all roles.");
        List<Role> allRole = roleRepository.findAll();

        if (allRole.isEmpty()) {
            LOG.warn("No role found.");
            return List.of();
        }

        LOG.info("Found " + allRole.size() + " role.");
        return allRole.stream()
                .map(this::mapToRoleResponse)
                .toList();
    }

    /**
     * Retrieves a role by its unique identifier.
     *
     * @param id the unique identifier of the role to retrieve.
     * @return an {@link Optional} containing a {@link RoleResponse} if the role exists,
     *         or an empty {@link Optional} if no role is found with the given ID.
     */
    @Transactional
    public Optional<RoleResponse> getRoleById(Long id) {
        LOG.info("Retrieving role with ID: " + id);
        Optional<Role> optionalRole = roleRepository.findById(Math.toIntExact(id));

        if (optionalRole.isPresent()) {
            LOG.info("Role found with ID: " + id);
            return Optional.of(mapToRoleResponse(optionalRole.get()));
        } else {
            LOG.warn("No role found with ID: " + id);
            return Optional.empty();
        }
    }

    /**
     * Updates an existing role in the system.
     *
     * @param id the unique identifier of the role to update.
     * @param roleRequest the data used to update the role, including optional fields for value, name, and description.
     * @return {@code true} if the role was successfully updated, {@code false} otherwise.
     * @throws Exception if no role is found with the given ID.
     */
    @Transactional
    public boolean updateRole(Long id, @Valid RoleRequest roleRequest) throws Exception {
        Optional<Role> optionalRole = this.roleRepository.findById(Math.toIntExact(id));
        if (optionalRole.isEmpty()) {
            LOG.warn("No role found with ID: " + id);
            throw new Exception("Role not found with id: " + id);
        }

        Role existingRole = optionalRole.get();
        if (roleRequest.getValue() != null) {
            existingRole.setValue(roleRequest.getValue());
        }

        if (roleRequest.getName() != null) {
            existingRole.setName(roleRequest.getName());
        }

        if (roleRequest.getDescription() != null) {
            existingRole.setDescription(roleRequest.getDescription());
        }

        existingRole.setActive(roleRequest.isActive());

        // Save the updated role
        roleRepository.save(existingRole);
        LOG.info("Role updated successfully with ID: " + id);
        return true;
    }

    /**
     * Deletes a role from the system by its unique identifier.
     *
     * @param id the unique identifier of the role to delete.
     * @throws Exception if no role is found with the given ID.
     */
    @Transactional
    public void deleteRole(Long id) throws Exception {
        LOG.info("Role with ID: " + id);
        Optional<Role> optionalRole = roleRepository.findById(Math.toIntExact(id));
        if (optionalRole.isEmpty()) {
            LOG.warn("No role found with ID: " + id);
            throw new Exception("Role not found with id: " + id);
        }
        roleRepository.deleteById(Math.toIntExact(id));
        LOG.info("Role deleted successfully with ID: " + id);
    }

    /**
     * Checks if a role exists by its value and name.
     *
     * @param value the value of the role.
     * @param name the name of the role.
     * @return true if a role exists with the specified value and name, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean existsByValueAndName(String value, String name) {
        LOG.info("Checking if role exists with value: " + value + " and name: " + name);
        boolean exists = roleRepository.findByValueAndName(value, name).isPresent();
        LOG.info("Role existence check result: " + exists);
        return exists;
    }

    /**
     * Converts a {@link Role} entity to a {@link RoleResponse} DTO.
     *
     * @param role the role entity to convert.
     * @return a {@link RoleResponse} object containing the data of the role entity.
     */
    private RoleResponse mapToRoleResponse(Role role) {
//        LOG.warn("role ********************* " + (role.getUsers() != null && !role.getUsers().isEmpty()) + "****************");
        return RoleResponse.builder()
                .id(role.getId())
                .value(role.getValue())
                .name(role.getName())
                .description(role.getDescription())
                .isActive(role.isActive())
                .userResponses(role.getUsers() != null && !role.getUsers().isEmpty()
                    ? role.getUsers().stream()
                        .map(this::toUserResponse)
                        .toList()
                    : Collections.emptyList())
                .build();
    }

    /**
     * Converts a {@link User} entity to a {@link UserResponse} DTO.
     *
     * @param entity the user entity to convert.
     * @return a {@link UserResponse} object containing the data of the user entity.
     */
    private UserResponse toUserResponse(User entity) {
        return UserResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .value(entity.getValue())
            .email(entity.getEmail())
            .isActive(entity.isActive())
            .phoneNumber(entity.getPhoneNumber())
            .dateOfBirth(entity.getDateOfBirth())
            .description(entity.getDescription())
            .dateLastLogin(entity.getDateLastLogin())
            .failedLoginCount(entity.getFailedLoginCount())
            .datePasswordChanged(entity.getDatePasswordChanged())
            .build();
    }
}
