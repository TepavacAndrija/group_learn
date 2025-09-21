package com.example.group_learn_project.room;

import com.example.group_learn_project.auth.JwtUtils;
import com.example.group_learn_project.user.User;
import com.example.group_learn_project.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public List<Room> getAllRooms(){
        return roomService.getAllRooms();
    }

    @GetMapping("/code/{code}")
    public Room getRoomByCode(@PathVariable String code){
        return roomService.getRoomByCode(code);
    }


    @PostMapping
    public Room createRoom(@RequestBody Map<String,String> body, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String id = jwtUtils.getIdFromToken(token);
        Room room = roomService.createRoom(body.get("packId"), id);
        messagingTemplate.convertAndSend("/topic/rooms", room);

        return room;
        //add check if duplicate to all post mappings, questionpack tooc
    }

    @PostMapping("/join")
    public Room joinRoom(@RequestBody Room room, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String id = jwtUtils.getIdFromToken(token);
        Map<String, String> response = new HashMap<>();
        response.put("code", room.getCode());
        messagingTemplate.convertAndSend("/user/queue/room-joined", response);
        return roomService.joinRoom(room.getCode(),id);
    }

    @PostMapping("/update")
    public Room updateAndBroadcast(@RequestBody Room room) {
        Room updated = roomService.updateRoom(room);

        messagingTemplate.convertAndSend("/topic/rooms", updated );

        return updated;
    }

    @GetMapping("/broadcast")
    public List<Room> broadcastAllRooms() {
        return roomService.getActiveRooms();
    }


}
