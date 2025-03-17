package com.winseslas.microservices.bookstore.UserManager.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winseslas.microservices.bookstore.UserManager.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByValueAndName(String value, String name);
}
