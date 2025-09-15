package com.example.group_learn_project.room;

import com.example.group_learn_project.questionpack.QuestionPack;
import com.example.group_learn_project.questionpack.QuestionPackService;
import com.example.group_learn_project.user.User;
import com.example.group_learn_project.user.UserRepository;
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
    @Autowired
    private UserRepository userRepository;

    public Room createRoom(Room room, String hostId){
        room.setHostId(hostId);
        room.setCode(generateRoomCode());
        room.setPlayerIds(Collections.singletonList(hostId));

        QuestionPack pack = questionPackService.findById(room.getPackId());

        if(pack != null){
            room.setPackName(pack.getName());
        }

        return roomRepository.save(room);
    }

    public String generateRoomCode(){
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public Room joinRoom(String code, String playerId){
        Room room = roomRepository.findByCode(code);
        if(room == null){
            throw new RuntimeException("Room not found");
        }
        if(room.getCurrentPlayers() >= room.getMaxPlayers()){
            throw new RuntimeException("Room is full");
        }
        if(room.getPlayerIds().contains(playerId)){
            return room;
        }

        room.getPlayerIds().add(playerId);
        room.setCurrentPlayers(room.getCurrentPlayers() + 1);
        Room updated = roomRepository.save(room);

        RoomUpdateDTO dto = new RoomUpdateDTO(updated);
        messagingTemplate.convertAndSend("/topic/rooms", dto);


        return updated;
    }

    public List<Room> getAllRooms(){
        return roomRepository.findAll();
    }

    public List<Room> getActiveRooms() {
        return roomRepository.findByStatus(RoomStatus.WAITING);
    }

    public Room getRoomById(String id) {
        return roomRepository.findById(id).orElseThrow();
    }

    public Room startGame(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setGameActive(true);
        room.setCurrentQuestionIndex(0);
        room.setCurrentAnswererId(room.getPlayerIds().get(0));
        return roomRepository.save(room);
    }

    public Room nextQuestion(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        List<String> players = room.getPlayerIds();
        int currentIndex = players.indexOf(room.getCurrentAnswererId());
        int nextIndex = (currentIndex + 1) % players.size();

        room.setCurrentQuestionIndex(room.getCurrentQuestionIndex() + 1);
        room.setCurrentAnswererId(players.get(nextIndex));
        return roomRepository.save(room);
    }

    public boolean isAnswerer(String roomId, String playerId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        return room.getCurrentAnswererId().equals(playerId);
    }

    public Room updateRoom(Room room) {
        return roomRepository.save(room);
    }
}
