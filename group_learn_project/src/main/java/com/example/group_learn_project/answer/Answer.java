package com.example.group_learn_project.answer;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "answers")
public class Answer {
    @Id
    private String id;
    private String roomId;
    private String playerId;
    private String questionId;
    private String text;
    private long timestamp = System.currentTimeMillis();
}
