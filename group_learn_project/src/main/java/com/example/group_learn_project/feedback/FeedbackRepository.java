package com.example.group_learn_project.feedback;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FeedbackRepository extends MongoRepository<Feedback, String> {
    List<Feedback> findByAnswerId(String answerId);
}