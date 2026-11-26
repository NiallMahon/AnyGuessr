package com.anyguessr.anyguessrbackend.identity.infrastructure;

import com.anyguessr.anyguessrbackend.identity.domain.ExternalLogin;
import com.anyguessr.anyguessrbackend.identity.domain.ExternalLoginRepository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores external login links in memory when the memory profile is active.
 * Data is lost when the application stops.
 */
@Profile("memory")
@Repository
public class InMemoryExternalLoginRepository implements ExternalLoginRepository {

    private final Map<String, ExternalLogin> externalLogins = new ConcurrentHashMap<>();

    @Override
    public Optional<ExternalLogin> find(String provider, String providerUserId) {
        return Optional.ofNullable(
                externalLogins.get(createKey(provider, providerUserId))
        );
    }

    @Override
    public ExternalLogin save(ExternalLogin externalLogin) {
        externalLogins.put(
                createKey(externalLogin.provider(), externalLogin.providerUserId()),
                externalLogin
        );

        return externalLogin;
    }

    private String createKey(String provider, String providerUserId) {
        return provider + ":" + providerUserId;
    }
}

