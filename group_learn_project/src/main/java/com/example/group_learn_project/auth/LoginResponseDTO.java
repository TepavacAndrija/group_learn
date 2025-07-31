package com.example.group_learn_project.auth;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    public LoginResponseDTO(String token) {
        this.token = token;
    }
}
