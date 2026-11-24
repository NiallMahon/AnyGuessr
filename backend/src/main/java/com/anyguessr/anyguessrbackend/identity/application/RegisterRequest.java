package com.anyguessr.anyguessrbackend.identity.application;
/**
 * Contains the details needed to register a local account.
 *
 * @param username the requested username
 * @param email the user's email address
 * @param password the password for the account
 * @param location the user's location
 */
public record RegisterRequest(
    String username,
    String email,
    String password,
    String location
) {
}

