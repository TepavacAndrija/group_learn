package com.example.group_learn_project.questionpack;

import com.example.group_learn_project.question.Question;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "questionpacks")
public class QuestionPack {
    @Id
    private String id;
    private String name;
    private List<Question> questions;
}
