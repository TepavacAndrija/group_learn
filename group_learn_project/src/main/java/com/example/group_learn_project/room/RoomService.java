package com.example.group_learn_project.room;

import com.example.group_learn_project.questionpack.QuestionPack;
import com.example.group_learn_project.questionpack.QuestionPackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private QuestionPackService questionPackService;

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
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms(){
        return roomRepository.findAll();
    }
    

}
