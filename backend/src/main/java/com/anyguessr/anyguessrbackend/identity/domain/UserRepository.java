// How users are saved and found; lookups ignore capital letters.
package com.anyguessr.anyguessrbackend.identity.domain;

import java.util.Optional;
/**
 * Defines how user accounts are stored and retrieved.
 */
public interface UserRepository {
    /**
     * Finds a user by email, ignoring case.
     *
     * @param email the email address to search for
     * @return the matching user, or an empty result if none exists
     */
    Optional<User> findByEmail(String email);
    /**
     * Finds a user by username, ignoring case.
     *
     * @param username the username to search for
     * @return the matching user, or an empty result if none exists
     */
    Optional<User> findByUsername(String username);
    /**
     * Finds a user by their local ID.
     *
     * @param id the user ID to search for
     * @return the matching user, or an empty result if none exists
     */
    Optional<User> findById(Long id);
        /**
     * Saves a user account.
     *
     * @param user the user to save
     * @return the saved user
     */
    User save(User user);
    /**
     * Indicates that a username or email address is already in use.
     */
    class UsernameOrEmailTakenException extends RuntimeException {
        public UsernameOrEmailTakenException(Throwable cause) {
            super("Username or email is already taken", cause);
        }
    }
}
