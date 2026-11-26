package com.anyguessr.anyguessrbackend.identity.infrastructure;


import com.anyguessr.anyguessrbackend.identity.domain.ExternalLogin;
import com.anyguessr.anyguessrbackend.identity.domain.ExternalLoginRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Profile("memory")
@Repository
public class InMemoryExternalLoginRepository implements ExternalLoginRepository {

    private final Map<String, ExternalLogin> externalLogins = new ConcurrentHashMap<>();

    @Override
    public Optional<ExternalLogin> find(String provider, String providerUserId) {
        if (provider == null || providerUserId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(externalLogins.get(key(provider, providerUserId)));
    }

    @Override
    public ExternalLogin save(ExternalLogin externalLogin) {
        externalLogins.put(
                key(externalLogin.provider(), externalLogin.providerUserId()),
                externalLogin
        );
        return externalLogin;
    }

    private String key(String provider, String providerUserId) {
        return provider.trim().toLowerCase() + "\u0000" + providerUserId;
    }
}