// How profiles are read; a profile is built from the user's row, so sign-up saves it with the user.
package com.anyguessr.anyguessrbackend.identity.domain;

import java.util.Optional;
public interface ProfileRepository {
    Optional<Profile> findById(Long id);
}
