package com.example.group_learn_project.correction;

import lombok.Data;

import java.util.List;

@Data
public class CorrectionDTO {
    String roomId;
    String questionId;
    List<CorrectionItem> corrections;

    @Data
    public static class CorrectionItem{
        String playerId;
        String text;
    }

}