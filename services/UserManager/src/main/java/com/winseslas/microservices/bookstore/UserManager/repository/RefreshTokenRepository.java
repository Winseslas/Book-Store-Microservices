package com.winseslas.microservices.bookstore.UserManager.repository;

import com.winseslas.microservices.bookstore.UserManager.model.entity.RefreshToken;
import com.winseslas.microservices.bookstore.UserManager.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    @Modifying
    void deleteByUser(User user);
}
