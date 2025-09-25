package com.example.group_learn_project.gameReports;

import com.example.group_learn_project.correction.CorrectionDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionReportDTO {
    private String questionId;
    private String questionText;
    private AnswerDTO answer;
    private List<AnswerDTO> corrections = new ArrayList<>();
}
