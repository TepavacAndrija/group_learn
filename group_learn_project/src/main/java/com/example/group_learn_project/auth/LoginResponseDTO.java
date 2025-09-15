package com.example.group_learn_project.auth;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private String userId;
    public LoginResponseDTO(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }
}
