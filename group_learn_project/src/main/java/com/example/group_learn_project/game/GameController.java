package com.example.group_learn_project.game;

import com.example.group_learn_project.answer.Answer;
import com.example.group_learn_project.answer.AnswerService;
import com.example.group_learn_project.auth.JwtUtils;
import com.example.group_learn_project.correction.CorrectionDTO;
import com.example.group_learn_project.correction.CorrectionService;
import com.example.group_learn_project.feedback.Feedback;
import com.example.group_learn_project.feedback.FeedbackService;
import com.example.group_learn_project.question.Question;
import com.example.group_learn_project.questionpack.QuestionPack;
import com.example.group_learn_project.questionpack.QuestionPackService;
import com.example.group_learn_project.room.Room;
import com.example.group_learn_project.room.RoomService;

import com.example.group_learn_project.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private UserService userService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private QuestionPackService questionPackService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CorrectionService correctionService;

    @PostMapping("/start")
    public ResponseEntity<?> startGame(@RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String roomId = body.get("roomId");
            String token = authHeader.substring(7);
            String playerId = jwtUtils.getIdFromToken(token);

            // Pokreni igru
            Room room = roomService.startGame(roomId, playerId);

            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/question/{roomId}")
    public Question getCurrentQuestion(@PathVariable String roomId) {
        Room room = roomService.getRoomById(roomId);
        QuestionPack pack = questionPackService.findById(room.getPackId());
        if (pack != null && pack.getQuestions() != null && !pack.getQuestions().isEmpty()) {
            int index = Math.min(room.getCurrentQuestionIndex(), pack.getQuestions().size() - 1);
            return pack.getQuestions().get(index);
        }
        return null;
    }

    @PostMapping("/answer")
    public Answer submitAnswer(@RequestBody Answer answer) {
        return answerService.saveAnswer(answer);
    }

    @PostMapping("/feedback")
    public Feedback submitFeedback(@RequestBody Feedback feedback) {
        return feedbackService.saveFeedback(feedback);
    }

    @GetMapping("/feedback/{answerId}")
    public List<Feedback> getFeedbacks(@PathVariable String answerId) {
        return feedbackService.getFeedbacksByAnswer(answerId);
    }

    @PostMapping("/next")
    public Room nextQuestion(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String authHeader) {
        String roomId = body.get("roomId");

        String token = authHeader.substring(7);
        String playerId = jwtUtils.getIdFromToken(token);

        return roomService.nextQuestion(roomId, playerId);
    }

    @GetMapping("/is-answerer")
    public Map<String, Boolean> isAnswerer(@RequestParam String roomId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String playerId = jwtUtils.getIdFromToken(token);

        boolean is = roomService.isAnswerer(roomId, playerId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAnswerer", is);
        return response;
    }

    @PostMapping("/corrections")
    public ResponseEntity<?> submitCorrections(@RequestBody CorrectionDTO dto) {
        try {
            correctionService.saveCorrections(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}