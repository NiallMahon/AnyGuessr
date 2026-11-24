package com.anyguessr.anyguessrbackend.identity.application;
/**
 * Contains the credentials needed to sign in to a local account.
 *
 * @param identifier the user's email address or username
 * @param password the account password
 */
public record LoginRequest(String identifier, String password) {
    
}
