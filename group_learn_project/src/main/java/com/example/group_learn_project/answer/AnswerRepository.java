package com.example.group_learn_project.answer;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AnswerRepository extends MongoRepository<Answer, String> {
    List<Answer> findByRoomId(String roomId);
    List<Answer> findByRoomIdAndPlayerId(String roomId, String playerId);
    Answer findByRoomIdAndQuestionId(String roomId, String questionId);
}