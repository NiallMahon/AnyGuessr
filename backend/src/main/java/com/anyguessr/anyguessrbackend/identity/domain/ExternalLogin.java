package com.anyguessr.anyguessrbackend.identity.domain;

public record ExternalLogin(
    String provider,
    String providerUserId,
    Long userId
) {
    
}
