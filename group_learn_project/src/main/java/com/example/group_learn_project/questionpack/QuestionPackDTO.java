package com.example.group_learn_project.questionpack;

import lombok.Data;

import java.util.List;

@Data
public class QuestionPackDTO {
    String name;
    List<String> questions;
}
