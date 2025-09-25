package com.example.group_learn_project.gameReports;

import com.example.group_learn_project.answer.Answer;
import com.example.group_learn_project.answer.AnswerService;
import com.example.group_learn_project.correction.Correction;
import com.example.group_learn_project.correction.CorrectionService;
import com.example.group_learn_project.question.Question;
import com.example.group_learn_project.questionpack.QuestionPack;
import com.example.group_learn_project.questionpack.QuestionPackService;
import com.example.group_learn_project.room.Room;
import com.example.group_learn_project.room.RoomService;
import com.example.group_learn_project.user.User;
import com.example.group_learn_project.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameReportService {


    @Autowired
    private RoomService roomService;

    @Autowired
    private QuestionPackService questionPackService;

    @Autowired
    private CorrectionService correctionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private UserService userService;

    public GameReportDTO generateReport (String roomId){
        Room room = roomService.getRoomById(roomId);
        if(room == null){
            throw new RuntimeException();
        }

        GameReportDTO gameReportDTO = new GameReportDTO();
        gameReportDTO.setRoomId(roomId);
        gameReportDTO.setPackName(room.getPackName());
        gameReportDTO.setGeneratedAt(System.currentTimeMillis());

        QuestionPack questionPack = questionPackService.findById(room.getPackId());
        List<Question> questions = questionPack.getQuestions();

        List<QuestionReportDTO> questionReports = new ArrayList<>();

        for(Question question : questions){
            QuestionReportDTO questionReportDTO = new QuestionReportDTO();
            questionReportDTO.setQuestionId(question.getId());
            questionReportDTO.setQuestionText(question.getText());

            List<Correction> corrections = correctionService.findCorrectionsByQuestionIdAndRoomId(question.getId(), roomId);
            for(Correction correction : corrections){
                AnswerDTO correctionDTO = new AnswerDTO();

                User playerCorrection = userService.getUserById(correction.getPlayerId()).orElse(null);
                if(playerCorrection == null){
                    throw new RuntimeException("GRESKA");
                }
                correctionDTO.setPlayerName(playerCorrection.getUsername());
                correctionDTO.setText(correction.getText());

                questionReportDTO.getCorrections().add(correctionDTO);
            }

            Answer answer = answerService.findByRoomIdAndQuestionId(roomId, question.getId());

            User playerAnswer = userService.getUserById(answer.getPlayerId()).orElse(null);
            if(playerAnswer == null){
                throw new RuntimeException("GRESKA");
            }
            AnswerDTO answerDTO = new AnswerDTO();
            answerDTO.setPlayerName(playerAnswer.getUsername());
            answerDTO.setText(answer.getText());
            questionReportDTO.setAnswer(answerDTO);

            questionReports.add(questionReportDTO);

        }

        gameReportDTO.setQuestions(questionReports);
        return gameReportDTO;

    }
}
