package com.winseslas.microservices.bookStore.UserManager.repository;

import com.winseslas.microservices.bookStore.UserManager.model.entitie.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
     Optional<User> findByEmail(String email);
}
