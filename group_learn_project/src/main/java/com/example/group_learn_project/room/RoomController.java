package com.example.group_learn_project.room;

import com.example.group_learn_project.auth.JwtUtils;
import com.example.group_learn_project.user.User;
import com.example.group_learn_project.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping
    public List<Room> getAllRooms(){
        return roomService.getAllRooms();
    }
    @PostMapping
    public Room createRoom(@RequestBody Room room, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String id = getIdFromToken(token);
        return roomService.createRoom(room, id);
        //add check if duplicate to all post mappings, questionpack too
    }

    @PostMapping("/join")
    public Room joinRoom(@RequestBody Room room, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String id = getIdFromToken(token);
        return roomService.joinRoom(room.getCode(),id);
    }

    public String getIdFromToken(String token){
        String hostEmail = jwtUtils.extractEmail(token);
        User user = userService.getUserByEmail(hostEmail).orElse(null);
        if (user != null) {
            return user.getId();
        }
        else  {
            return null;
        }
    }
}
