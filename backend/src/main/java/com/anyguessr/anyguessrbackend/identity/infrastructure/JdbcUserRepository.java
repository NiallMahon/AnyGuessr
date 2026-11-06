// Saves and finds users in the MySQL users table. MySQL hands out the IDs, so they survive restarts and are never reused.
package com.anyguessr.anyguessrbackend.identity.infrastructure;

import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private static final String SELECT_USER =
            "SELECT user_id, email, password_hash, username, location FROM users ";

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        return findOne(SELECT_USER + "WHERE LOWER(email) = ?", lowerCase(email));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }

        return findOne(SELECT_USER + "WHERE LOWER(username) = ?", lowerCase(username));
    }

    @Override
    public User save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbc.update(connection -> {
                PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO users (username, email, password_hash, location, created_at) "
                                + "VALUES (?, ?, ?, ?, ?)",
                        new String[] {"user_id"});
                insert.setString(1, user.username());
                insert.setString(2, user.email());
                insert.setString(3, user.passwordHash());
                insert.setString(4, user.location());
                insert.setTimestamp(5, Timestamp.from(Instant.now()));
                return insert;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new UsernameOrEmailTakenException(e);
        }

        return new User(
                keyHolder.getKey().longValue(),
                user.email(),
                user.passwordHash(),
                user.username(),
                user.location()
        );
    }

    private Optional<User> findOne(String sql, String value) {
        List<User> users = jdbc.query(sql, JdbcUserRepository::toUser, value);
        return users.stream().findFirst();
    }

    private static User toUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("username"),
                rs.getString("location")
        );
    }

    private static String lowerCase(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
