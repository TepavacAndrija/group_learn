package com.example.group_learn_project.room;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Data
@Document(collection = "rooms")
public class Room {
    @Id
    private String id;
    private String code;
    private String packId;
    private String packName;
    private Integer maxPlayers = 6;
    private Integer currentPlayers = 1;
    private Boolean isActive = true;
    private String hostId;
    private List<String> playerIds = new ArrayList<>();
    private Integer currentQuestionIndex = 0;
    private String currentAnswererId;
    private Boolean gameActive = false;
    private RoomStatus status = RoomStatus.WAITING;

}
