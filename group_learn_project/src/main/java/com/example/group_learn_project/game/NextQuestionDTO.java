package com.example.group_learn_project.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class NextQuestionDTO {
    String code;
    Integer currentAnswererId;
    String currentQuestionIndex;
}
