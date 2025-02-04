package com.winseslas.microservices.bookStore.UserManager.service;

import com.winseslas.microservices.bookStore.UserManager.model.dto.BPartnerRequest;
import com.winseslas.microservices.bookStore.UserManager.model.dto.BPartnerResponse;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.BPartner;
import com.winseslas.microservices.bookStore.UserManager.repository.BPartnerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BPartnerService {

    @PersistenceContext
    private EntityManager entityManager;

    private final BPartnerRepository bpartnerRepository;
    private static final Log LOG = LogFactory.getLog(BPartnerService.class);

    public BPartnerService(BPartnerRepository partnerRepository) {
        this.bpartnerRepository = partnerRepository;
    }

    /**
     * Creates a new business partners.
     *
     * @param bpartnerRequest the request body containing the new business partners details.
     * @return a {@link BPartnerResponse} containing the created business partners details.
     */
    @Transactional
    public BPartnerResponse createBPartner(@Valid BPartnerRequest bpartnerRequest) {
        BPartner newBusinessPartners = BPartner.builder()
                .value(bpartnerRequest.getValue())
                .name(bpartnerRequest.getName())
                .description(bpartnerRequest.getDescription())
                .isActive(true)
                .build();
        bpartnerRepository.save(newBusinessPartners);
        LOG.info("Business Partners created successfully with ID: " + newBusinessPartners.getId());
        return mapToBPartnerResponse(newBusinessPartners);
    }

    /**
     * Retrieves all Business Partners from the repository.
     *
     * @return a list of BPartnerResponse objects representing all
     *         available Business Partners. Returns an empty list if no
     *         categories are found.
     */
    @Transactional
    public List<BPartnerResponse> getAllBusinessPartners() {
        LOG.info("Retrieving all Business Partners.");
        List<BPartner> allBPartner = bpartnerRepository.findAll();

        if (allBPartner.isEmpty()) {
            LOG.warn("No Business Partners found.");
            return List.of();
        }

        LOG.info("Found " + allBPartner.size() + " business partners.");
        return allBPartner.stream()
                .map(this::mapToBPartnerResponse)
                .toList();
    }

    /**
     * Retrieves a business partners by its ID.
     *
     * @param id the ID of the business partners to retrieve.
     * @return an Optional containing the business partners if found, or an empty
     *         Optional if not found.
     */
    @Transactional
    public Optional<BPartnerResponse> getBPartnerById(Long id) {
        LOG.info("Retrieving business partners with ID: " + id);
        Optional<BPartner> optionalBusinessPartners = bpartnerRepository.findById(Math.toIntExact(id));

        if (optionalBusinessPartners.isPresent()) {
            LOG.info("Business Partners found with ID: " + id);
            return Optional.of(mapToBPartnerResponse(optionalBusinessPartners.get()));
        } else {
            LOG.warn("No business partners found with ID: " + id);
            return Optional.empty();
        }
    }

    /**
     * Finds Business Partners by a specific field name and value.
     *
     * @param fieldName the name of the field to search by
     * @param value     the value to match
     * @return a list of Business Partners matching the criteria
     */
    @Transactional
    public List<BPartnerResponse> findBusinessPartnersByField(String fieldName, Object value) {
        LOG.info("Searching for Business Partners by field: " + fieldName + ", value: " + value);

        // Construct a dynamic JPQL query
        String queryStr = "SELECT pc FROM BPartner pc WHERE pc." + fieldName + " = :value";
        TypedQuery<BPartner> query = entityManager.createQuery(queryStr, BPartner.class);
        query.setParameter("value", value);

        // Execute the query and retrieve the result list
        List<BPartner> categories = query.getResultList();
        if (categories.isEmpty()) {
            LOG.warn("No Business Partners found.");
            return List.of();
        }

        LOG.info("Found " + categories.size() + " business partners for field: " + fieldName);
        return categories.stream()
                .map(this::mapToBPartnerResponse)
                .toList();
    }

    @Transactional
    public boolean updateBPartner(Long id, BPartnerRequest bpartnerRequest) throws Exception {
        Optional<BPartner> optionalBusinessPartners = this.bpartnerRepository.findById(Math.toIntExact(id));
        if (optionalBusinessPartners.isEmpty()) {
            LOG.warn("No business partners found with ID: " + id);
            throw new Exception("Business Partners not found with id: " + id);
        }

        BPartner existingBusinessPartners = optionalBusinessPartners.get();
        existingBusinessPartners.setValue(bpartnerRequest.getValue());
        existingBusinessPartners.setName(bpartnerRequest.getName());

        if (bpartnerRequest.getDescription() != null) {
            existingBusinessPartners.setDescription(bpartnerRequest.getDescription());
        }
        // Save the updated business partners
        bpartnerRepository.save(existingBusinessPartners);
        LOG.info("Business Partners updated successfully with ID: " + id);
        return true;
    }

    /**
     * Deletes a business partners by its ID.
     *
     * @param id the ID of the business partners to delete.
     * @throws Exception if the business partners is not found.
     */
    @Transactional
    public void deleteBPartner(Long id) throws Exception {
        LOG.info("Deleting business partners with ID: " + id);
        Optional<BPartner> optionalBusinessPartners = bpartnerRepository.findById(Math.toIntExact(id));
        if (optionalBusinessPartners.isEmpty()) {
            LOG.warn("No business partners found with ID: " + id);
            throw new Exception("Business Partners not found with id: " + id);
        }
        bpartnerRepository.deleteById(Math.toIntExact(id));
        LOG.info("Business Partners deleted successfully with ID: " + id);
    }

    /**
     * Maps a BPartner object to a BPartnerResponse object.
     *
     * @param bpartner the BPartner object to map.
     * @return a BPartnerResponse object.
     */
    private BPartnerResponse mapToBPartnerResponse(BPartner bpartner) {
        return BPartnerResponse.builder()
                .id(bpartner.getId())
                .value(bpartner.getValue())
                .name(bpartner.getName())
                .description(bpartner.getDescription())
                .isActive(bpartner.isActive())
                .isAuthor(bpartner.getIsAuthor())
                .isCustomer(bpartner.getIsCustomer())
                .isEmployee(bpartner.getIsEmployee())
                .profileUrl(bpartner.getProfileUrl())
                .gender(bpartner.getGender())
                .build();
    }
}
