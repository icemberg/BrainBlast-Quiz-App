package com.icemberg.QuizApplication.service.interfaces;

import java.util.List;

public interface RedisService {
    List<Integer> getSeenQuestionIds(String userId, String category);
    void addSeenQuestionIds(String userId, String category, List<Integer> questionIds);
    void clearSeenQuestions(String userId, String category);
    void saveQuizSession(String sessionId, List<Integer> questionIds, String title);
    List<Integer> getQuizSession(String sessionId);
    String getQuizTitle(String sessionId);
}
