package com.example.group_learn_project.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameUpdateDTO {
    String type;
    String code;
    String currentAnswererId;
    int currentQuestionIndex;

    public GameUpdateDTO(){}
}
