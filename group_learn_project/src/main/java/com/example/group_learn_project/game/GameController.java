package com.example.group_learn_project.game;

import com.example.group_learn_project.answer.Answer;
import com.example.group_learn_project.answer.AnswerService;
import com.example.group_learn_project.auth.JwtUtils;
import com.example.group_learn_project.correction.CorrectionDTO;
import com.example.group_learn_project.correction.CorrectionService;
import com.example.group_learn_project.gameReports.GameReportDTO;
import com.example.group_learn_project.gameReports.GameReportService;
import com.example.group_learn_project.question.Question;
import com.example.group_learn_project.questionpack.QuestionPack;
import com.example.group_learn_project.questionpack.QuestionPackService;
import com.example.group_learn_project.room.QuestionPhase;
import com.example.group_learn_project.room.Room;
import com.example.group_learn_project.room.RoomRepository;
import com.example.group_learn_project.room.RoomService;

import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
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
    private QuestionPackService questionPackService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CorrectionService correctionService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameReportService reportService;
    @Autowired
    private GameReportService gameReportService;

    @GetMapping("/report/{roomId}")
    public ResponseEntity<?> getReport(@PathVariable String roomId) {
        GameReportDTO report = reportService.generateReport(roomId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/start")
    public ResponseEntity<?> startGame(@RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String roomId = body.get("roomId");
            String playerId = jwtUtils.getIdFromHeader(authHeader);

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
        Answer newAnswer = answerService.saveAnswer(answer);

        Room room = roomService.getRoomById(answer.getRoomId());
        room.setQuestionPhase(QuestionPhase.CORRECTING);
        room.setPlayersWhoSubmitted(new HashSet<>());

        roomRepository.save(room);

        messagingTemplate.convertAndSend(
                "/topic/game/" + room.getCode(),
                new GameUpdateDTO("CORRECTION_PHASE", room.getCode(), room.getCurrentAnswererId(),newAnswer.getText(),
                        room.getCurrentQuestionIndex()));

        return newAnswer;
    }

    @PostMapping("/next")
    public Room nextQuestion(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String authHeader) {
        String roomId = body.get("roomId");

        String playerId = jwtUtils.getIdFromHeader(authHeader);

        return roomService.nextQuestion(roomId, playerId);
    }

    @GetMapping("/is-answerer")
    public Map<String, Boolean> isAnswerer(@RequestParam String roomId,
            @RequestHeader("Authorization") String authHeader) {
        String playerId = jwtUtils.getIdFromHeader(authHeader);

        boolean is = roomService.isAnswerer(roomId, playerId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAnswerer", is);
        return response;
    }

    @PostMapping("/correction")
    public ResponseEntity<?> submitCorrections(@RequestBody CorrectionDTO dto) {
        try {
            correctionService.saveCorrection(dto);

            Room room = roomService.getRoomById(dto.getRoomId());

            if (!room.getPlayersWhoSubmitted().contains(dto.getPlayerId())) {
                room.getPlayersWhoSubmitted().add(dto.getPlayerId());
                roomRepository.save(room);
            }
            List<String> players = room.getPlayerIds();
            String currentAnswererId = room.getCurrentAnswererId();

            List<String> playersToSubmit = players.stream()
                    .filter(id -> !id.equals(currentAnswererId))
                    .toList();

            boolean allSubmitted = playersToSubmit.size() == room.getPlayersWhoSubmitted().size();

            if (allSubmitted) {
                Room nextRoom = roomService.nextQuestion(dto.getRoomId(), dto.getPlayerId());

                messagingTemplate.convertAndSend(
                        "/topic/game/" + nextRoom.getCode(),
                        new GameUpdateDTO("NEXT_QUESTION", nextRoom.getCode(), nextRoom.getCurrentAnswererId(),
                                nextRoom.getCurrentQuestionIndex()));
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("GRESKA JE " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("GRESKA JE:", e.getMessage()));
        }
    }

    @GetMapping("/report/{roomId}/pdf")
    public void downloadPdfReport(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String authHeader,
            HttpServletResponse response
    ) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        String playerId = jwtUtils.getIdFromHeader(authHeader);

        Room room = roomService.getRoomById(roomId);
        if (!room.getPlayerIds().contains(playerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not part of this room");
        }

        GameReportDTO report = gameReportService.generateReport(roomId);

        InputStream reportStream = getClass().getResourceAsStream("/reports/game_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("roomId", report.getRoomId());
        parameters.put("packName", report.getPackName());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(report.getQuestions());

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=game_report_" + roomId + ".pdf");
        JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
        response.getOutputStream().flush();
    }

}