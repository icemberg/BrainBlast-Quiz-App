package com.icemberg.QuizApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.icemberg.QuizApplication.entity.Quiz;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    
    List<Quiz> findByIsTemplateTrue();
    
    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM quiz_questions WHERE quiz_id = (SELECT id FROM quiz WHERE category = :category ORDER BY id ASC LIMIT 1)", nativeQuery = true)
    long countQuestionsByQuizCategory(String category);
}
