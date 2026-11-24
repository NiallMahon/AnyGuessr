package com.anyguessr.anyguessrbackend.identity.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.security.oauth2.client.registration.cognito.client-id=test-client",
                "spring.security.oauth2.client.registration.cognito.client-secret=test-secret",
                "spring.security.oauth2.client.registration.cognito.authorization-grant-type=authorization_code",
                "spring.security.oauth2.client.registration.cognito.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}",
                "spring.security.oauth2.client.registration.cognito.scope=openid",
                "spring.security.oauth2.client.provider.cognito.authorization-uri=https://example.com/authorize",
                "spring.security.oauth2.client.provider.cognito.token-uri=https://example.com/token",
                "spring.security.oauth2.client.provider.cognito.jwk-set-uri=https://example.com/jwks"
        }
)
@ActiveProfiles("memory")
class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void registerReturnsCreatedUserId() throws Exception {
        String json = """
                {
                  "username": "integration-user",
                  "email": "integration@example.com",
                  "password": "test-password",
                  "location": "Galway"
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().matches("\\s*\\{\\s*\"userId\"\\s*:\\s*\\d+\\s*}\\s*"));
    }
}