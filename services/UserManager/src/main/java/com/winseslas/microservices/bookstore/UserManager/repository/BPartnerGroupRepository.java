package com.winseslas.microservices.bookstore.UserManager.repository;

import com.winseslas.microservices.bookstore.UserManager.model.entity.BPartnerGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BPartnerGroupRepository extends JpaRepository<BPartnerGroup, Integer> {
}
