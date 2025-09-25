package com.example.group_learn_project.gameReports;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameReportDTO {
    private String roomId;
    private String packName;
    private List<QuestionReportDTO> questions = new ArrayList<>();
    private long generatedAt;

}
