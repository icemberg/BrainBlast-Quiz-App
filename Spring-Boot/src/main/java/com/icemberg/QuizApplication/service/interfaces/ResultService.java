package com.icemberg.QuizApplication.service.interfaces;

import com.icemberg.QuizApplication.dto.AnswerVerificationResponse;
import com.icemberg.QuizApplication.dto.Response;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface ResultService {
    ResponseEntity<String> calculateScore(String id, List<Response> responses);
    AnswerVerificationResponse verifyAnswer(Integer questionId, String selectedAnswer);
}

