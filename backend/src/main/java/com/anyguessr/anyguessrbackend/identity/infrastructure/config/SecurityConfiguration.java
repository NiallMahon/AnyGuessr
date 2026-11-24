package com.anyguessr.anyguessrbackend.identity.infrastructure.config;

import com.anyguessr.anyguessrbackend.identity.application.CognitoUserProvisioningService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Class to configure AWS Cognito as an OAuth 2.0 authorizer with Spring Security.
 * In this configuration, we specify our OAuth Client.
 * We also declare that all requests must come from an authenticated user.
 * Finally, we configure our logout handler.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CognitoUserProvisioningService provisioningService) throws Exception {
        CognitoLogoutHandler cognitoLogoutHandler = new CognitoLogoutHandler();

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/auth/**"))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/api/**", "/auth/**", "/error").permitAll()
                        .anyRequest()
                        .authenticated())

                        .oauth2Login(oauth2 -> oauth2
                            .failureHandler((request, response, exception) -> {
                            response.sendRedirect("http://localhost:5173/login?error=oauth2");
                        })
                        .successHandler((request, response, authentication) -> {
                            OidcUser cognitoUser = (OidcUser) authentication.getPrincipal();                            
                            Long userId = provisioningService.findOrCreateUser(cognitoUser);
                            
                            request.getSession().setAttribute("userId", userId);
                            response.sendRedirect("http://localhost:5173/profile");
                        })
                )
                .logout(logout -> logout.logoutSuccessHandler(cognitoLogoutHandler));
        return http.build();
    }
}
