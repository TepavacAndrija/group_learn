package com.example.group_learn_project.auth;

import com.example.group_learn_project.user.User;
import com.example.group_learn_project.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        System.out.println("=== LOGIN REQUEST ===");
        System.out.println("Email: " + request.getEmail());
        System.out.println(
                "Password length: " + (request.getPassword() != null ? request.getPassword().length() : "null"));

        try {
            String email = request.getEmail();
            String token = authService.login(email, request.getPassword());
            System.out.println("Login successful for " + email);
            System.out.println("JWT Token generated (first 50 chars): "
                    + token.substring(0, Math.min(50, token.length())) + "...");
            User user = userService.getUserByEmail(email).orElseThrow();
            String playerId = user.getId();
            return ResponseEntity.ok(new LoginResponseDTO(token, email, playerId));
        } catch (AuthenticationException e) {
            System.out.println("Login failed for: " + request.getEmail() + " -> " + e.getMessage());
            return ResponseEntity.status(401).body("Wrong email or password");
        }
        // kasnije premestiti logistiku za login u sevice da bude lepse
    }

}
