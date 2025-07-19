package com.example.group_learn_project.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register (@RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok("Registred succesfullyS");
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login (@RequestBody LoginRequestDTO request) {
       try {
           Authentication authentication = authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
           SecurityContextHolder.getContext().setAuthentication(authentication);
           UserDetails userDetails = (UserDetails) authentication.getPrincipal();
           String email = userDetails.getUsername();
           String token = jwtUtils.generateToken(email);
           return ResponseEntity.ok(token);
           //trebao bih napraviti dto da vraca
       }catch (AuthenticationException e){
           return ResponseEntity.status(401).body("Wrong email or password");
       }
       //kasnije premestiti logistiku za login u sevice da bude lepse
    }
}
