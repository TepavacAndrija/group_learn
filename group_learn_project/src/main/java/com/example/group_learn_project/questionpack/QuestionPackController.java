package com.example.group_learn_project.questionpack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packs")
public class QuestionPackController {

    @Autowired
    private QuestionPackService questionPackService;

    @GetMapping
    public List<QuestionPack> findAll(){
        return questionPackService.findAll();
    }

    @PostMapping
    public QuestionPack save(@RequestBody QuestionPack questionPack){
        return questionPackService.create(questionPack);
    }
}
