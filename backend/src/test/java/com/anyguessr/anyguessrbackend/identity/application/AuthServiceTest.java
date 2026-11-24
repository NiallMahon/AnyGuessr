package com.anyguessr.anyguessrbackend.identity.application;

import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void registerSavesUserWithEncodedPasswordAndReturnsId() {
        // Arrange
        AuthService authService = new AuthService(userRepository, passwordEncoder);

        RegisterRequest request = new RegisterRequest(
                "cillian",
                "cillian@example.com",
                "plain-password",
                "Galway"
        );

        when(userRepository.findByEmail("cillian@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByUsername("cillian"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    return new User(
                            42L,
                            user.username(),
                            user.email(),
                            user.passwordHash(),
                            user.location()
                    );
                });

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals("encoded-password", userCaptor.getValue().passwordHash());
        assertEquals(42L, response.userId());
    }
}
