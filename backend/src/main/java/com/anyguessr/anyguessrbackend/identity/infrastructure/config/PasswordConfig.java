package com.anyguessr.anyguessrbackend.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
/**
 * Configures BCrypt password encoding for local accounts.
 * 
 */
@Configuration
public class PasswordConfig {
    /**
     * Creates the encoder used to hash and verify local account passwords.
     *
     * @return a BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
