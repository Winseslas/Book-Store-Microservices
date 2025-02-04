package com.winseslas.microservices.bookStore.UserManager.repository;

import com.winseslas.microservices.bookStore.UserManager.model.entitie.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByValueAndName(String value, String name);
}
