package com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.repository;

import com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/**
 * 
 * Provides database access to user entities using Spring Data JPA.
 */
public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {
    /**
     * Finds a user by email, ignoring case.
     *
     * @param email the email address to search for
     * @return the matching user, or an empty result if none exists
     */
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    /**
     * Finds a user by username, ignoring case.
     *
     * @param username the username to search for
     * @return the matching user, or an empty result if none exists
     */
    Optional<UserEntity> findByUsernameIgnoreCase(String username);

}
