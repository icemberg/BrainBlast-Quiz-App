package com.icemberg.QuizApplication.service.interfaces;

import com.icemberg.QuizApplication.entity.Question;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface QuestionService {
    ResponseEntity<List<Question>> getAllQuestions();
    ResponseEntity<List<Question>> getQuestionsByCategory(String category);
    ResponseEntity<String> addQuestion(Question question);
    ResponseEntity<String> updateQuestion(Integer id, Question question);
    ResponseEntity<String> deleteQuestion(Integer id);
}
