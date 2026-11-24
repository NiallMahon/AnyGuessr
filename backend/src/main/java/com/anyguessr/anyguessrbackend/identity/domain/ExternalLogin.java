package com.anyguessr.anyguessrbackend.identity.domain;
/**
 * Represents a link between an external identity and a local user account.
 *
 * @param provider the external provider, such as Cognito
 * @param providerUserId the user's ID at the external provider
 * @param userId the corresponding local user ID
 */
public record ExternalLogin(
    String provider,
    String providerUserId,
    Long userId
) {
    
}
