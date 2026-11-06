package com.anyguessr.anyguessrbackend.identity.domain;

import com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity.UserEntity;

public record User(
    Long id,
    String username,
    String email,
    String passwordHash
){   
}