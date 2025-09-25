package com.example.group_learn_project.correction;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorrectionRepository extends MongoRepository<Correction, String> {
    List<Correction> findCorrectionsByQuestionIdAndRoomId(String questionId, String roomId);
    List<Correction> findCorrectionByQuestionId(String id);
}
