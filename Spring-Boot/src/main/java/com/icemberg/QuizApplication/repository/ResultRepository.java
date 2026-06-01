package com.icemberg.QuizApplication.repository;

import com.icemberg.QuizApplication.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Integer> {
    
    List<Result> findTop5ByOrderBySubmissionDateDesc();

    @Query("SELECT AVG((r.score * 100.0) / r.totalQuestions) FROM Result r")
    Double findAverageScorePercentage();
    
    @Query("SELECT COUNT(DISTINCT r.username) FROM Result r")
    Long countActiveUsers();
}
