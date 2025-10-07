package com.example.group_learn_project.questionpack;

import com.example.group_learn_project.question.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public QuestionPack create(QuestionPackDTO questionPack)
    {
        List<Question> questions = new ArrayList<>();
        if(questionPack.getQuestions()!=null){
            questionPack.getQuestions().forEach(q->{
                Question newQuestion = new Question();
                newQuestion.setId(UUID.randomUUID().toString());
                newQuestion.setText(q);
                questions.add(newQuestion);
            });
        }
        QuestionPack newQuestionPack = new QuestionPack();
        newQuestionPack.setName(questionPack.getName());
        newQuestionPack.setQuestions(questions);
        return questionPackRepository.save(newQuestionPack);
    }

    public QuestionPack findById(String id)
    {
        return questionPackRepository.findById(id).orElse(null);
    }
}
