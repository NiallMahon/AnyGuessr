// How users are saved and found; lookups ignore capital letters.
package com.anyguessr.anyguessrbackend.identity.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);

    User save(User user);

    class UsernameOrEmailTakenException extends RuntimeException {
        public UsernameOrEmailTakenException(Throwable cause) {
            super("Username or email is already taken", cause);
        }
    }
}
