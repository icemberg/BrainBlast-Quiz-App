package com.icemberg.QuizApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerVerificationRequest {
    private Integer questionId;
    private String selectedAnswer;
}
