package com.example.group_learn_project.auth;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private String email;
    private String playerId;
    public LoginResponseDTO(String token,String email, String playerId) {
        this.token = token;
        this.email = email;
        this.playerId = playerId;
    }
}
