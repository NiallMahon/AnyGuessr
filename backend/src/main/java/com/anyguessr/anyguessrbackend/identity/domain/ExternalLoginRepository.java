package com.anyguessr.anyguessrbackend.identity.domain;

import java.util.Optional;

public interface ExternalLoginRepository {
    
    Optional<ExternalLogin> find(
        String provider,
        String providerUserId
    );

    ExternalLogin save (ExternalLogin externalLogin);
}
