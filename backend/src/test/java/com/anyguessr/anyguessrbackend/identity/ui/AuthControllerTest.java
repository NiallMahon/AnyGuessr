// Checks the URLs the Register and Login pages call.
package com.anyguessr.anyguessrbackend.identity.ui;

import com.anyguessr.anyguessrbackend.identity.application.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource("classpath:identity-test.properties")
@Transactional
class AuthControllerTest {

    @Autowired
    private AuthService authService;

    @Test
    void registerPageUrlCreatesAnAccountThatCanLogIn() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();

        String registered = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "sam", "email": "sam@example.com",
                                 "password": "secret123", "location": "Cork, Ireland"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andReturn().getResponse().getContentAsString();

        String loggedIn = mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier": "sam@example.com", "password": "secret123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(registered, loggedIn);
    }
}
