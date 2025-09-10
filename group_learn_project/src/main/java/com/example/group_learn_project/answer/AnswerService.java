package com.example.group_learn_project.answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository repository;

    public Answer saveAnswer(Answer answer) {
        return repository.save(answer);
    }

    public List<Answer> getAnswersByRoom(String roomId) {
        return repository.findByRoomId(roomId);
    }
}