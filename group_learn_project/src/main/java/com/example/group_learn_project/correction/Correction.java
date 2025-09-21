package com.example.group_learn_project.correction;

import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "corrections")
@AllArgsConstructor
@RequiredArgsConstructor
public class Correction {
    @Id
    private String id;
    private String questionId;
    private String playerId;
    private String text;

    public Correction(String questionId, String playerId, String text) {
        this.questionId = questionId;
        this.playerId = playerId;
        this.text = text;
    }
}
