package com.example.group_learn_project.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NextQuestionDTO {
    String code;
    Integer currentAnswererId;
    String currentQuestionIndex;
}
