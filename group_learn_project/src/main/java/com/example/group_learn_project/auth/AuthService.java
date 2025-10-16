package com.example.group_learn_project.auth;

import com.example.group_learn_project.user.User;
import com.example.group_learn_project.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    public void register(RegisterRequestDTO registerRequestDTO) {

        validate(registerRequestDTO);

        User user = new User();
        user.setUsername(registerRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setEmail(registerRequestDTO.getEmail());
        userService.saveUser(user);
    }

    private void validate(RegisterRequestDTO registerRequestDTO) {
        if(registerRequestDTO.getUsername() == null || registerRequestDTO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if(registerRequestDTO.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password is required to be at least 8 characters");
        }
        if(registerRequestDTO.getEmail() == null || registerRequestDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if(!registerRequestDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }

    public LoginResponseDTO login (String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails.getUsername());

            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return new LoginResponseDTO(token,email,user.getId());
        } catch (AuthenticationException e) {
            throw new AuthenticationException("Invalid email or password", e) {};
        }
    }

}
