package com.anyguessr.anyguessrbackend.identity.domain;

import java.util.Optional;
/**
 * Defines how external login links are stored and retrieved.
 */
public interface ExternalLoginRepository {
    /**
     * Finds an external login by provider and provider user ID.
     *
     * @param provider the name of the external provider
     * @param providerUserId the user's ID at that provider
     * @return the matching login link, or an empty result if none exists
     */
    Optional<ExternalLogin> find(
        String provider,
        String providerUserId
    );
    /**
     * Saves an external login link.
     *
     * @param externalLogin the login link to save
     * @return the saved login link
     */
    ExternalLogin save (ExternalLogin externalLogin);
}
