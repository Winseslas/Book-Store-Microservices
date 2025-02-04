package com.winseslas.microservices.bookStore.UserManager.repository;

import com.winseslas.microservices.bookStore.UserManager.model.entitie.BPartner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BPartnerRepository extends JpaRepository<BPartner, Integer> {
}
