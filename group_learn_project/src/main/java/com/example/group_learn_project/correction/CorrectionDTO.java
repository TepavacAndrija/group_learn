package com.example.group_learn_project.correction;

import lombok.Data;

import java.util.List;

@Data
public class CorrectionDTO {
    String roomId;
    String questionId;
    String playerId;
    String text;

}