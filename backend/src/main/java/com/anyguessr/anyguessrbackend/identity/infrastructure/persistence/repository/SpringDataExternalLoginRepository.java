package com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.repository;

import com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity.ExternalLoginEntity;
import com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity.ExternalLoginId;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Provides database access to external login entities using Spring Data JPA.
 */
public interface SpringDataExternalLoginRepository extends JpaRepository<ExternalLoginEntity, ExternalLoginId>{

}
