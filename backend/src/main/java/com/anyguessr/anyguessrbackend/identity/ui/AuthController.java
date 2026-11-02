package com.anyguessr.anyguessrbackend.identity.ui;

import com.anyguessr.anyguessrbackend.identity.application.AuthResponse;
import com.anyguessr.anyguessrbackend.identity.application.AuthService;
import com.anyguessr.anyguessrbackend.identity.application.LoginRequest;
import com.anyguessr.anyguessrbackend.identity.application.RegisterRequest;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
//RequestMapping("/auth")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(
            @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request
    )   {
        return authService.login(request);
    }
}