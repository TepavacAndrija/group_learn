package com.example.group_learn_project.room;

import com.example.group_learn_project.game.GameFinishedDTO;
import com.example.group_learn_project.game.GameUpdateDTO;
import com.example.group_learn_project.game.NextQuestionDTO;
import com.example.group_learn_project.questionpack.QuestionPack;
import com.example.group_learn_project.questionpack.QuestionPackService;
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

    public Room createRoom(String packId, String hostId){
        Room room = new Room();
        QuestionPack pack = questionPackService.findById(packId);
        if(pack != null){
            room.setPackId(packId);
            room.setPackName(pack.getName());
        }
        else{
            throw new RuntimeException("Pack id not found");
        }
        room.setHostId(hostId);
        room.setCode(generateRoomCode());
        room.setPlayerIds(Collections.singletonList(hostId));

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

    public Room getRoomByCode(String code) {
        return roomRepository.findByCode(code);
    }


//    public Room startGame(String roomId, String playerId) {
//        Room room = roomRepository.findById(roomId).orElseThrow();
//
//        if (!room.getHostId().equals(playerId)) {
//            throw new RuntimeException("Only host can start the game");
//        }
//
//        room.setStatus(RoomStatus.ACTIVE);
//        room.setCurrentQuestionIndex(0);
//        room.setCurrentAnswererId(room.getPlayerIds().getFirst());
//        Room updated = roomRepository.save(room);
//
//        messagingTemplate.convertAndSend("/topic/game/" + room.getCode(),
//                new GameStartedDTO(room room.getCode(), room.getCurrentAnswererId()));
//
//        return updated;
//    }

    public Room startGame(String roomId, String playerId) {
        // 1. Dohvati sobu iz baze
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        // 2. Proveri da li je korisnik host sobe
        if (!room.getHostId().equals(playerId)) {
            throw new RuntimeException("Only the host can start the game.");
        }


        room.setStatus(RoomStatus.ACTIVE);
        room.setCurrentQuestionIndex(0);

        // Proveri da li ima igrača u sobi pre nego što pokušaš da uzmeš prvog
        List<String> playerIds = room.getPlayerIds();
        if (playerIds == null || playerIds.isEmpty()) {
            throw new RuntimeException("Cannot start game: No players in the room.");
        }
        // Postavi prvog igrača kao trenutnog odgovarača
        room.setCurrentAnswererId(playerIds.getFirst()); // ili .get(0) ako koristiš stariju Javu

        // 5. Sačuvaj ažuriranu sobu u bazi
        Room updatedRoom = roomRepository.save(room);

        // 6. Pripremi DTO poruku za WebSocket
        GameUpdateDTO message = new GameUpdateDTO();
        message.setType("GAME_STARTED");
        message.setCode(room.getCode());
        message.setCurrentAnswererId(room.getCurrentAnswererId());
        message.setCurrentQuestionIndex(room.getCurrentQuestionIndex());


        // 7. Pošalji poruku svim klijentima pretplaćenim na /topic/game/{code}
        // Ovo će ih obavestiti da je igra počela i ko je prvi odgovarač
        messagingTemplate.convertAndSend(
                "/topic/game/" + updatedRoom.getCode(),
                message
        );

        // 8. Opciono: Pošalji ažuriranje i na /topic/room/{code} ako frontend to koristi
        // za ažuriranje stanja sobe (npr. WAITING -> ACTIVE)
        // messagingTemplate.convertAndSend("/topic/room/" + updatedRoom.getCode(), updatedRoom);

        // 9. Vrati ažuriranu sobu
        return updatedRoom;
    }

//    public Room nextQuestion(String roomId, String playerId) {
//        Room room = roomRepository.findById(roomId).orElseThrow();
//
//        if (!room.getCurrentAnswererId().equals(playerId)) {
//            throw new RuntimeException("Only current answerer can move to next question");
//        }
//        QuestionPack pack = questionPackService.findById(room.getPackId());
//        if (room.getCurrentQuestionIndex() >= pack.getQuestions().size() - 1) {
//            room.setStatus(RoomStatus.FINISHED);
//            Room updated = roomRepository.save(room);
//
//            // Emituj kraj igre
//            messagingTemplate.convertAndSend("/topic/game/" + room.getCode(),
//                    new GameFinishedDTO(room.getCode()));
//
//            return updated;
//        }
//
//        List<String> players = room.getPlayerIds();
//        int currentIndex = players.indexOf(room.getCurrentAnswererId());
//        int nextIndex = (currentIndex + 1) % players.size();
//
//        room.setCurrentQuestionIndex(room.getCurrentQuestionIndex() + 1);
//        room.setCurrentAnswererId(players.get(nextIndex));
//
//        Room updated = roomRepository.save(room);
//
//        // Emituj novi odgovarač i pitanje
//        messagingTemplate.convertAndSend("/topic/game/" + room.getCode(),
//                new NextQuestionDTO(
//                        room.getCode(),
//                        room.getCurrentQuestionIndex(),
//                        room.getCurrentAnswererId()
//                ));
//
//        return updated;
//    }

    // RoomService.java (ili gde god se ova metoda nalazi)

    public Room nextQuestion(String roomId, String playerId) {
        // 1. Dohvati sobu iz baze
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        // 2. Proveri da li je korisnik trenutni odgovarač
        if (!room.getCurrentAnswererId().equals(playerId)) {
            throw new RuntimeException("Only current answerer can move to next question");
        }

        // 3. Dohvati paket pitanja
        QuestionPack pack = questionPackService.findById(room.getPackId());
        if (pack == null) {
            throw new RuntimeException("Question pack not found for room");
        }

        // 4. Proveri da li je ovo poslednje pitanje (ili već prošao kraj)
        // Pretpostavimo da getCurrentQuestionIndex() počinje od 0
        // i da getQuestions().size() daje ukupan broj pitanja.
        if (room.getCurrentQuestionIndex() >= pack.getQuestions().size() - 1) {
            // 4a. Ako jeste, igra je gotova
            room.setStatus(RoomStatus.FINISHED); // Pretpostavljamo da postoji FINISHED status
            Room updated = roomRepository.save(room);

            // 4b. Pripremi i pošalji DTO poruku o kraju igre
            GameUpdateDTO gameOverMessage = new GameUpdateDTO();
            gameOverMessage.setType("GAME_FINISHED");
            gameOverMessage.setCode(room.getCode());
            gameOverMessage.setCurrentAnswererId(null); // Nema više odgovarača
            gameOverMessage.setCurrentQuestionIndex(room.getCurrentQuestionIndex()); // Ili -1 ako želiš

            messagingTemplate.convertAndSend("/topic/game/" + room.getCode(), gameOverMessage);

            return updated;
        }

        // 5. Ako nije kraj, pređi na sledeće pitanje
        room.setCurrentQuestionIndex(room.getCurrentQuestionIndex() + 1);

        // 6. Rotiraj odgovarača
        List<String> players = room.getPlayerIds();
        if (players == null || players.isEmpty()) {
            // Ovo ne bi trebalo da se desi u ispravnoj igri, ali radi sigurnosti...
            throw new RuntimeException("Cannot rotate answerer: No players in room");
        }
        int currentIndex = players.indexOf(room.getCurrentAnswererId());
        if (currentIndex == -1) {
            // Trenutni odgovarač više nije u sobi? Ili greška...
            throw new RuntimeException("Current answerer not found in player list");
        }
        int nextIndex = (currentIndex + 1) % players.size();
        room.setCurrentAnswererId(players.get(nextIndex));

        // 7. Sačuvaj ažuriranja u bazi
        Room updated = roomRepository.save(room);

        // 8. Pripremi i pošalji DTO poruku o sledećem pitanju
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
