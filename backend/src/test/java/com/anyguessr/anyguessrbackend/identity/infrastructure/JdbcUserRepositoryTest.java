// Saves and finds users in the users table (H2 test database built from the real schema).
package com.anyguessr.anyguessrbackend.identity.infrastructure;

import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository.UsernameOrEmailTakenException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("classpath:identity-test.properties")
@Transactional
class JdbcUserRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static User newUser(String username, String email) {
        return new User(null, email, "hash", username, "Galway, Ireland");
    }

    @Test
    void databaseGivesEachUserANewId() {
        JdbcUserRepository repository = new JdbcUserRepository(jdbcTemplate);

        User first = repository.save(newUser("alice", "alice@example.com"));
        User second = repository.save(newUser("bob", "bob@example.com"));

        assertNotNull(first.id());
        assertTrue(second.id() > first.id());
    }

    @Test
    void newRepositoryStillFindsTheUserLikeAfterARestart() {
        Long userId = new JdbcUserRepository(jdbcTemplate)
                .save(newUser("alice", "alice@example.com")).id();

        JdbcUserRepository afterRestart = new JdbcUserRepository(jdbcTemplate);

        assertEquals(userId, afterRestart.findByEmail("alice@example.com").orElseThrow().id());
    }

    @Test
    void idOfARemovedUserIsNotGivenOutAgain() {
        JdbcUserRepository repository = new JdbcUserRepository(jdbcTemplate);
        Long removedId = repository.save(newUser("alice", "alice@example.com")).id();
        jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", removedId);

        Long nextId = repository.save(newUser("bob", "bob@example.com")).id();

        assertTrue(nextId > removedId);
    }

    @Test
    void findsByEmailOrUsernameWhateverTheCapitals() {
        JdbcUserRepository repository = new JdbcUserRepository(jdbcTemplate);
        Long userId = repository.save(newUser("Alice", "alice@example.com")).id();

        assertEquals(userId, repository.findByEmail(" ALICE@Example.com ").orElseThrow().id());
        assertEquals(userId, repository.findByUsername("alice").orElseThrow().id());
        assertTrue(repository.findByUsername("nobody").isEmpty());
    }

    @Test
    void databaseRejectsATakenUsername() {
        JdbcUserRepository repository = new JdbcUserRepository(jdbcTemplate);
        repository.save(newUser("alice", "alice@example.com"));

        assertThrows(UsernameOrEmailTakenException.class,
                () -> repository.save(newUser("alice", "other@example.com")));
    }
}
