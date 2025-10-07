package com.example.group_learn_project.questionpack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packs")
public class QuestionPackController {

    @Autowired
    private QuestionPackService questionPackService;

    @GetMapping
    public List<QuestionPack> findAll() {
        return questionPackService.findAll();
    }

    @PostMapping
    public QuestionPack save(@RequestBody QuestionPackDTO questionPack) {
        return questionPackService.create(questionPack);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionPack> getPackById(@PathVariable String id) {
        QuestionPack pack = questionPackService.findById(id);
        return ResponseEntity.ok(pack);
    }

}
