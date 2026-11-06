// Group-level test: sign-up and login through AuthService and the database, then the profile.
package com.anyguessr.anyguessrbackend.identity.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("classpath:identity-test.properties")
@Transactional
class AuthServiceTest {

    private static final String PASSWORD = "secret123";

    @Autowired
    private AuthService authService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static RegisterRequest signUp(String username, String email) {
        return new RegisterRequest(username, email, PASSWORD, "Cork, Ireland");
    }

    @Test
    void newPlayerCanLogInWithEmailOrUsername() {
        Long userId = authService.register(signUp("sam", "sam@example.com")).userId();

        assertEquals(userId, authService.login(new LoginRequest("sam@example.com", PASSWORD)).userId());
        assertEquals(userId, authService.login(new LoginRequest("sam", PASSWORD)).userId());
    }

    @Test
    void passwordIsStoredAsAHashNotAsTyped() {
        Long userId = authService.register(signUp("sam", "sam@example.com")).userId();

        String stored = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE user_id = ?", String.class, userId);

        assertNotEquals(PASSWORD, stored);
        assertTrue(stored.startsWith("$2"));
    }

    @Test
    void wrongPasswordIsRejected() {
        authService.register(signUp("sam", "sam@example.com"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("sam@example.com", "wrong")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void takenEmailIsRejectedWhateverTheCapitals() {
        authService.register(signUp("sam", "sam@example.com"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.register(signUp("someone-else", "SAM@example.com")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("An account with this email already exists", error.getReason());
    }

    @Test
    void takenUsernameIsRejectedWhateverTheCapitals() {
        authService.register(signUp("Sam", "sam@example.com"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.register(signUp("sam", "other@example.com")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("That username is already taken", error.getReason());
    }

    @Test
    void newPlayerHasAProfile() {
        Long userId = authService.register(signUp("Sam Player", "sam@example.com")).userId();

        UserProfileResponse profile = profileService.getProfile(userId);

        assertEquals("Sam Player", profile.name());
        assertEquals("Cork, Ireland", profile.location());
        assertEquals("SP", profile.initials());
    }

    @Test
    void starterAccountIsUserOneAndCanLogIn() {
        assertEquals(1L, authService.login(new LoginRequest("cillian@email.com", "password")).userId());
        assertEquals("CillianShine", profileService.getProfile(1L).name());
    }
}
