package com.example.group_learn_project.room;

import com.example.group_learn_project.game.GameUpdateDTO;
import com.example.group_learn_project.questionpack.QuestionPack;
import com.example.group_learn_project.questionpack.QuestionPackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private QuestionPackService questionPackService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Room createRoom(String packId, String hostId) {
        Room room = new Room();
        QuestionPack pack = questionPackService.findById(packId);
        if (pack != null) {
            room.setPackId(packId);
            room.setPackName(pack.getName());
        } else {
            throw new RuntimeException("Pack id not found");
        }
        room.setHostId(hostId);
        room.setCode(generateRoomCode());
        room.setPlayerIds(Collections.singletonList(hostId));

        return roomRepository.save(room);
    }

    public String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public Room joinRoom(String code, String playerId) {
        Room room = roomRepository.findByCode(code);
        if (room == null) {
            throw new RuntimeException("Room not found");
        }
        if (room.getCurrentPlayers() >= room.getMaxPlayers()) {
            throw new RuntimeException("Room is full");
        }
        if (room.getPlayerIds().contains(playerId)) {
            return room;
        }

        room.getPlayerIds().add(playerId);
        room.setCurrentPlayers(room.getCurrentPlayers() + 1);
        Room updated = roomRepository.save(room);

        RoomUpdateDTO dto = new RoomUpdateDTO(updated);
        messagingTemplate.convertAndSend("/topic/rooms", dto);

        return updated;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getActiveRooms() {
        return roomRepository.findByStatus(RoomStatus.WAITING);
    }

    public Room getRoomById(String id) {
        return roomRepository.findById(id).orElseThrow();
    }

    public Room getRoomByCode(String code) {
        return roomRepository.findByCode(code);
    }
    

    public Room startGame(String roomId, String playerId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        if (!room.getHostId().equals(playerId)) {
            throw new RuntimeException("Only the host can start the game.");
        }

        room.setStatus(RoomStatus.ACTIVE);
        room.setCurrentQuestionIndex(0);

        List<String> playerIds = room.getPlayerIds();
        if (playerIds == null || playerIds.isEmpty()) {
            throw new RuntimeException("Cannot start game: No players in the room.");
        }

        room.setCurrentAnswererId(playerIds.getFirst());

        Room updatedRoom = roomRepository.save(room);

        GameUpdateDTO message = new GameUpdateDTO();
        message.setType("GAME_STARTED");
        message.setCode(room.getCode());
        message.setCurrentAnswererId(room.getCurrentAnswererId());
        message.setCurrentQuestionIndex(room.getCurrentQuestionIndex());

        messagingTemplate.convertAndSend(
                "/topic/game/" + updatedRoom.getCode(),
                message);

        return updatedRoom;
    }

    public Room nextQuestion(String roomId, String playerId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        QuestionPack pack = questionPackService.findById(room.getPackId());
        if (pack == null) {
            throw new RuntimeException("Question pack not found for room");
        }

        if (room.getCurrentQuestionIndex() >= pack.getQuestions().size() - 1) {

            room.setStatus(RoomStatus.FINISHED);
            Room updated = roomRepository.save(room);

            GameUpdateDTO gameOverMessage = new GameUpdateDTO();
            gameOverMessage.setType("GAME_FINISHED");
            gameOverMessage.setCode(room.getCode());
            gameOverMessage.setCurrentAnswererId(null);
            gameOverMessage.setCurrentQuestionIndex(room.getCurrentQuestionIndex());

            messagingTemplate.convertAndSend("/topic/game/" + room.getCode(), gameOverMessage);

            return updated;
        }

        room.setCurrentQuestionIndex(room.getCurrentQuestionIndex() + 1);

        List<String> players = room.getPlayerIds();
        if (players == null || players.isEmpty()) {
            throw new RuntimeException("Cannot rotate answerer: No players in room");
        }
        int currentIndex = players.indexOf(room.getCurrentAnswererId());
        if (currentIndex == -1) {
            throw new RuntimeException("Current answerer not found in player list");
        }
        int nextIndex = (currentIndex + 1) % players.size();
        room.setCurrentAnswererId(players.get(nextIndex));


        Room updated = roomRepository.save(room);

        GameUpdateDTO nextQuestionMessage = new GameUpdateDTO();
        nextQuestionMessage.setType("NEXT_QUESTION");
        nextQuestionMessage.setCode(room.getCode());
        nextQuestionMessage.setCurrentAnswererId(room.getCurrentAnswererId());
        nextQuestionMessage.setCurrentQuestionIndex(room.getCurrentQuestionIndex());

        messagingTemplate.convertAndSend("/topic/game/" + room.getCode(), nextQuestionMessage);

        return updated;
    }

    public boolean isAnswerer(String roomId, String playerId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        return room.getCurrentAnswererId().equals(playerId);
    }

    public Room updateRoom(Room room) {
        return roomRepository.save(room);
    }
}
