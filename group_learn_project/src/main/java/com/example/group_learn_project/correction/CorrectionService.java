package com.example.group_learn_project.correction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CorrectionService {

    @Autowired
    private CorrectionRepository correctionRepository;

    public void saveCorrections(CorrectionDTO dto) {
        List<Correction> correctionsToSave = dto.getCorrections().stream()
                .filter(item -> item != null &&
                        item.getPlayerId() != null && !item.getPlayerId().isEmpty() &&
                        item.getText() != null && !item.getText().trim().isEmpty())
                .map(item -> new Correction(dto.getQuestionId(), item.getPlayerId(), item.getText().trim()))
                .collect(Collectors.toList());

        if (!correctionsToSave.isEmpty()) {
            correctionRepository.saveAll(correctionsToSave);
            System.out.println("Saved " + correctionsToSave.size() + " corrections for question ID: " + dto.getQuestionId());
        } else {
            System.out.println("No valid corrections found to save for question ID: " + dto.getQuestionId());
        }
    }
}

