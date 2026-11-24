package com.anyguessr.anyguessrbackend.identity.application;
/**
 * Contains the result of registering or authenticating a local user.
 *
 * @param userId the local ID of the registered or authenticated user
 */
public record AuthResponse(Long userId) {
}
