package com.example.group_learn_project.room;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomUpdateDTO {
    private String id;
    private String code;
    private String packName;
    private Integer currentPlayers;
    private Integer maxPlayers;
    private RoomStatus status;

    public RoomUpdateDTO(Room room) {
        this.id = room.getId();
        this.code = room.getCode();
        this.packName = room.getPackName();
        this.currentPlayers = room.getCurrentPlayers();
        this.maxPlayers = room.getMaxPlayers();
        this.status = room.getStatus();
    }
}
