package com.example.group_learn_project.feedback;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "feedbacks")
public class Feedback {
    @Id
    private String id;
    private String answerId;
    private String playerId;
    private String text;
    private long timestamp = System.currentTimeMillis();
}