package com.example.group_learn_project.room;

import com.example.group_learn_project.auth.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/code/{code}")
    public Room getRoomByCode(@PathVariable String code) {
        return roomService.getRoomByCode(code);
    }

    @PostMapping
    public Room createRoom(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String authHeader) {
        String id = jwtUtils.getIdFromHeader(authHeader);
        Room room = roomService.createRoom(body.get("packId"), id);
        messagingTemplate.convertAndSend("/topic/rooms", room);

        return room;
    }

    @PostMapping("/join")
    public Room joinRoom(@RequestBody Room room, @RequestHeader("Authorization") String authHeader) {
        String id = jwtUtils.getIdFromHeader(authHeader);
        Map<String, String> response = new HashMap<>();
        response.put("code", room.getCode());
        messagingTemplate.convertAndSend("/user/queue/room-joined", response);
        return roomService.joinRoom(room.getCode(), id);
    }

    @PostMapping("/update")
    public Room updateAndBroadcast(@RequestBody Room room) {
        Room updated = roomService.updateRoom(room);

        messagingTemplate.convertAndSend("/topic/rooms", updated);

        return updated;
    }

    @GetMapping("/broadcast")
    public List<Room> broadcastAllRooms() {
        return roomService.getActiveRooms();
    }

}
