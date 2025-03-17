package com.winseslas.microservices.bookstore.UserManager.repository;

import com.winseslas.microservices.bookstore.UserManager.model.entity.BPartner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BPartnerRepository extends JpaRepository<BPartner, Integer> {
}
