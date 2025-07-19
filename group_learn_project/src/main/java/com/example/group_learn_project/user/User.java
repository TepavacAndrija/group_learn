package com.example.group_learn_project.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//enkripciju dodati
//enum role dodati, dodatne funkcionalnosti za admine
@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
}
