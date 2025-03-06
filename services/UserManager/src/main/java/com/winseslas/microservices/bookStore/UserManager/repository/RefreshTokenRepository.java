package com.winseslas.microservices.bookStore.UserManager.repository;

import com.winseslas.microservices.bookStore.UserManager.model.entitie.RefreshToken;
import com.winseslas.microservices.bookStore.UserManager.model.entitie.User;
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
