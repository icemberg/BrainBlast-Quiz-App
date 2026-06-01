package com.icemberg.QuizApplication.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.icemberg.QuizApplication.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    
    List<Question> findByCategory(String category);

    @Query(value = "SELECT * FROM question WHERE category = ?1 ORDER BY RAND() LIMIT ?2", nativeQuery = true)
    List<Question> findRandomQuestionsByCategory(String category, int numQ);

    @Query(value = "SELECT * FROM question WHERE category = ?1 AND id NOT IN (?3) ORDER BY RAND() LIMIT ?2", nativeQuery = true)
    List<Question> findRandomQuestionsByCategoryAndExcludeIds(String category, int numQ, List<Integer> excludedIds);

    boolean existsByQuestionTitleAndCategory(String questionTitle, String category);

    long countByCategoryAndDifficultylevel(String category, String difficultylevel);


}
