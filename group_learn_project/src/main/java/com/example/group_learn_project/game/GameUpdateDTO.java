package com.example.group_learn_project.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameUpdateDTO {
    String type;
    String code;
    String currentAnswererId;
    String info;
    int currentQuestionIndex;

    public GameUpdateDTO(String type, String code, String currentAnswererId,  int currentQuestionIndex) {
        this.type = type;
        this.code = code;
        this.currentAnswererId = currentAnswererId;
        this.currentQuestionIndex = currentQuestionIndex;
        this.info = "";
    }
}
