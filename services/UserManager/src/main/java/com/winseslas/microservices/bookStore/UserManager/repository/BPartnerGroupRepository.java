package com.winseslas.microservices.bookStore.UserManager.repository;

import com.winseslas.microservices.bookStore.UserManager.model.entitie.BPartnerGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BPartnerGroupRepository extends JpaRepository<BPartnerGroup, Integer> {
}
