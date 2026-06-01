package com.icemberg.QuizApplication.dto;

import lombok.Data;

@Data
public class QuizCorrectionRequest {
    private String question;
    private String userAnswer;
    private String correctAnswer;
}
