package com.winseslas.microservices.bookStore.UserManager.service;

import com.winseslas.microservices.bookStore.UserManager.model.dto.BPartnerGroupRequest;
import com.winseslas.microservices.bookStore.UserManager.model.dto.BPartnerGroupResponse;
import com.winseslas.microservices.bookStore.UserManager.model.dto.BPartnerResponse;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.BPartner;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.BPartnerGroup;
import com.winseslas.microservices.bookStore.UserManager.repository.BPartnerGroupRepository;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Business Partner Groups.
 * This service provides methods to create, retrieve, update, and delete Business Partner Groups
 * as well as map their entities to response DTOs.
 */
@Service
public class BPartnerGroupService {

    private final BPartnerGroupRepository bpartnerGroupRepository;
    private static final Log LOG = LogFactory.getLog(BPartnerGroupService.class);

    public BPartnerGroupService(BPartnerGroupRepository bpartnerGroupRepository) {
        this.bpartnerGroupRepository = bpartnerGroupRepository;
    }

    /**
     * Creates a new Business Partner Group.
     *
     * @param bPartnerGroupRequest the request object containing details of the new Business Partner Group.
     * @return a {@link BPartnerGroupResponse} with details of the created Business Partner Group.
     */
    @Transactional
    public BPartnerGroupResponse createBPartnerGroup(@Valid BPartnerGroupRequest bPartnerGroupRequest) {
        BPartnerGroup newBusinessGroupPartners = BPartnerGroup.builder()
                .value(bPartnerGroupRequest.getValue())
                .name(bPartnerGroupRequest.getName())
                .description(bPartnerGroupRequest.getDescription())
                .isActive(true)
                .isAuthor(bPartnerGroupRequest.getIsAuthor())
                .isCustomer(bPartnerGroupRequest.getIsCustomer())
                .isEmployee(bPartnerGroupRequest.getIsEmployee())
                .build();
        bpartnerGroupRepository.save(newBusinessGroupPartners);
        LOG.info("Business Partner Group created successfully with ID: " + newBusinessGroupPartners.getId());
        return mapToBPartnerGroupResponse(newBusinessGroupPartners);
    }

    /**
     * Retrieves all Business Partner Groups.
     *
     * @return a list of {@link BPartnerGroupResponse} objects for all Business Partner Groups.
     */
    @Transactional
    public List<BPartnerGroupResponse> getAllBusinessPartnersGroup() {
        LOG.info("Retrieving all Business Partner Groups.");
        List<BPartnerGroup> allBPartnerGroup = bpartnerGroupRepository.findAll();

        if (allBPartnerGroup.isEmpty()) {
            LOG.warn("No Business Partner Groups found.");
            return List.of();
        }

        LOG.info("Found " + allBPartnerGroup.size() + " Business Partner Groups.");
        return allBPartnerGroup.stream()
                .map(this::mapToBPartnerGroupResponse)
                .toList();
    }

    /**
     * Retrieves a Business Partner Group by its ID.
     *
     * @param id the ID of the Business Partner Group to retrieve.
     * @return an {@link Optional} containing the {@link BPartnerGroupResponse} if found.
     */
    @Transactional
    public Optional<BPartnerGroupResponse> getBPartnerGroupById(Long id) {
        LOG.info("Retrieving Business Partner Group with ID: " + id);
        Optional<BPartnerGroup> optionalBusinessPartnersGroup = bpartnerGroupRepository.findById(Math.toIntExact(id));

        if (optionalBusinessPartnersGroup.isPresent()) {
            LOG.info("Business Partner Group found with ID: " + id);
            return Optional.of(mapToBPartnerGroupResponse(optionalBusinessPartnersGroup.get()));
        } else {
            LOG.warn("No Business Partner Group found with ID: " + id);
            return Optional.empty();
        }
    }

    /**
     * Updates a Business Partner Group.
     *
     * @param id                   the ID of the Business Partner Group to update.
     * @param bpartnerGroupRequest the updated details for the Business Partner Group.
     * @return {@code true} if the update was successful.
     * @throws Exception if the Business Partner Group is not found.
     */
    @Transactional
    public boolean updateBPartnerGroup(Long id, BPartnerGroupRequest bpartnerGroupRequest) throws Exception {
        Optional<BPartnerGroup> optionalBusinessPartnersGroup = bpartnerGroupRepository.findById(Math.toIntExact(id));
        if (optionalBusinessPartnersGroup.isEmpty()) {
            LOG.warn("No Business Partner Group found with ID: " + id);
            throw new Exception("Business Partner Group not found with ID: " + id);
        }

        BPartnerGroup existingBusinessPartnersGroup = optionalBusinessPartnersGroup.get();
        existingBusinessPartnersGroup.setValue(bpartnerGroupRequest.getValue());
        existingBusinessPartnersGroup.setName(bpartnerGroupRequest.getName());

        if (bpartnerGroupRequest.getDescription() != null) {
            existingBusinessPartnersGroup.setDescription(bpartnerGroupRequest.getDescription());
        }

        bpartnerGroupRepository.save(existingBusinessPartnersGroup);
        LOG.info("Business Partner Group updated successfully with ID: " + id);
        return true;
    }

    /**
     * Deletes a Business Partner Group by its ID.
     *
     * @param id the ID of the Business Partner Group to delete.
     * @throws Exception if the Business Partner Group is not found.
     */
    @Transactional
    public void deleteBPartnerGroup(Long id) throws Exception {
        LOG.info("Deleting Business Partner Group with ID: " + id);
        Optional<BPartnerGroup> optionalBusinessPartners = bpartnerGroupRepository.findById(Math.toIntExact(id));
        if (optionalBusinessPartners.isEmpty()) {
            LOG.warn("No Business Partner Group found with ID: " + id);
            throw new Exception("Business Partner Group not found with ID: " + id);
        }
        bpartnerGroupRepository.deleteById(Math.toIntExact(id));
        LOG.info("Business Partner Group deleted successfully with ID: " + id);
    }

    /**
     * Maps a {@link BPartnerGroup} entity to a {@link BPartnerGroupResponse} DTO.
     *
     * @param bPartnerGroup the {@link BPartnerGroup} entity to map.
     * @return the corresponding {@link BPartnerGroupResponse}.
     */
    private BPartnerGroupResponse mapToBPartnerGroupResponse(BPartnerGroup bPartnerGroup) {
        return BPartnerGroupResponse.builder()
                .id(bPartnerGroup.getId())
                .value(bPartnerGroup.getValue())
                .name(bPartnerGroup.getName())
                .description(bPartnerGroup.getDescription())
                .isActive(bPartnerGroup.isActive())
                .isAuthor(bPartnerGroup.getIsAuthor())
                .isCustomer(bPartnerGroup.getIsCustomer())
                .isEmployee(bPartnerGroup.getIsEmployee())
                .bpartners(bPartnerGroup.getBpartners() != null
                        ? bPartnerGroup.getBpartners().stream()
                        .map(this::toBPartnerResponse)
                        .toList()
                        : Collections.emptyList())
                .build();
    }

    /**
     * Converts a BPartner entity to a BPartnerResponse DTO.
     * This method maps the essential fields from the BPartner entity to a new BPartnerResponse object.
     *
     * @param entity The BPartner entity to be converted.
     * @return A BPartnerResponse object containing the mapped data from the BPartner entity.
     */
    private BPartnerResponse toBPartnerResponse(BPartner entity) {
        return BPartnerResponse.builder()
                .id(entity.getId())
                .value(entity.getValue())
                .name(entity.getName())
                .isActive(entity.isActive())
                .profileUrl(entity.getProfileUrl())
                .gender(entity.getGender())
                .build();
    }
}
