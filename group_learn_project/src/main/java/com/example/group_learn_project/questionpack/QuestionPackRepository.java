package com.example.group_learn_project.questionpack;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionPackRepository extends MongoRepository<QuestionPack,String> {
    List<QuestionPack> findAll();
}
