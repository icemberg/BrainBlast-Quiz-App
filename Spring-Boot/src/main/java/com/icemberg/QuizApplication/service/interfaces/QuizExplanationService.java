package com.icemberg.QuizApplication.service.interfaces;

import com.icemberg.QuizApplication.dto.ExplanationResponse;

public interface QuizExplanationService {
    ExplanationResponse generateFailureExplanation(String question, String wrongAnswer, String correctAnswer);
}
