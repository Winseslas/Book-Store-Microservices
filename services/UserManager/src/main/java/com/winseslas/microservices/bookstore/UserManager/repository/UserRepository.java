package com.winseslas.microservices.bookstore.UserManager.repository;

import com.winseslas.microservices.bookstore.UserManager.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
     Optional<User> findByEmail(String email);
}
