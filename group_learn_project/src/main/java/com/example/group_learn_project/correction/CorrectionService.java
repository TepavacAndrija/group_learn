package com.example.group_learn_project.correction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CorrectionService {

    @Autowired
    private CorrectionRepository correctionRepository;

    public void saveCorrection(CorrectionDTO dto) {
        Correction correction = new Correction();
        correction.setPlayerId(dto.getPlayerId());
        correction.setQuestionId(dto.getQuestionId());
        correction.setText(dto.getText().trim());
        correction.setRoomId(dto.getRoomId());
        correctionRepository.save(correction);

    }

    public List<Correction> findCorrectionsByQuestionIdAndRoomId(String questionId, String roomId) {
        return correctionRepository.findCorrectionsByQuestionIdAndRoomId(questionId, roomId);
    }
}

