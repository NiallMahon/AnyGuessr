// Builds a user's profile from their row in the MySQL users table.
package com.anyguessr.anyguessrbackend.identity.infrastructure;

import com.anyguessr.anyguessrbackend.identity.domain.Profile;
import com.anyguessr.anyguessrbackend.identity.domain.ProfileRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcProfileRepository implements ProfileRepository {

    private final JdbcTemplate jdbc;

    public JdbcProfileRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Profile> findById(Long id) {
        List<Profile> profiles = jdbc.query(
                "SELECT user_id, username, location FROM users WHERE user_id = ?",
                (rs, rowNum) -> new Profile(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getString("location"),
                        Profile.initialsOf(rs.getString("username")),
                        0),
                id);

        return profiles.stream().findFirst();
    }
}
