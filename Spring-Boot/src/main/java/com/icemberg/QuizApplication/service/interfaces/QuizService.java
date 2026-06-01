package com.icemberg.QuizApplication.service.interfaces;

import com.icemberg.QuizApplication.entity.Quiz;
import com.icemberg.QuizApplication.dto.QuestionWrapper;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface QuizService {
    ResponseEntity<?> createQuiz(String category, int numQ, String title);
    ResponseEntity<List<QuestionWrapper>> getQuizQuestions(String id);
    ResponseEntity<List<Quiz>> getAllQuizzes();
    ResponseEntity<Long> getQuestionCount(String category);
    String performCleanup();
}
