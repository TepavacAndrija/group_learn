package com.example.group_learn_project.questionpack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class QuestionPackService
{
    @Autowired
    private QuestionPackRepository questionPackRepository;

    public List<QuestionPack> findAll()
    {
        return questionPackRepository.findAll();
    }

    public QuestionPack create(QuestionPack questionPack)
    {
        if(questionPack.getQuestions()!=null){
            questionPack.getQuestions().forEach(q->{
                if(q.getId()==null){
                    q.setId(UUID.randomUUID().toString());
                }
            });
        }
        return questionPackRepository.save(questionPack);
    }

    public QuestionPack findById(String id)
    {
        return questionPackRepository.findById(id).orElse(null);
    }
}
