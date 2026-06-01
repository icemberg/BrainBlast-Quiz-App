package com.icemberg.QuizApplication.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.icemberg.QuizApplication.service.interfaces.RedisService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    // Key pattern: quiz:seen:{userId}:{category}
    private static final String SEEN_KEY_PREFIX = "quiz:seen:";
    private static final long TTL_HOURS = 12;
    private static final int MAX_SEEN_SIZE = 200;

    @Override
    public List<Integer> getSeenQuestionIds(String userId, String category) {
        String key = SEEN_KEY_PREFIX + userId + ":" + category;
        Set<String> members = redisTemplate.opsForSet().members(key);
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }
        return members.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    @Override
    public void addSeenQuestionIds(String userId, String category, List<Integer> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) return;

        String key = SEEN_KEY_PREFIX + userId + ":" + category;
        String[] values = questionIds.stream().map(String::valueOf).toArray(String[]::new);
        
        redisTemplate.opsForSet().add(key, values);
        redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
    }

    @Override
    public void clearSeenQuestions(String userId, String category) {
        String key = SEEN_KEY_PREFIX + userId + ":" + category;
        redisTemplate.delete(key);
    }

    // --- Session Management (For Stateless Quizzes) ---
    private static final String SESSION_KEY_PREFIX = "quiz:session:";

    @Override
    public void saveQuizSession(String sessionId, List<Integer> questionIds, String title) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String ids = questionIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        // Format: title####id1,id2,id3
        String data = title + "####" + ids;
        redisTemplate.opsForValue().set(key, data, 1, TimeUnit.HOURS); // Session lasts 1 hour
    }

    @Override
    public List<Integer> getQuizSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String data = redisTemplate.opsForValue().get(key);
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Handle legacy format (just IDs) or new format (title####IDs)
        String idsPart = data;
        if (data.contains("####")) {
            String[] parts = data.split("####");
            if (parts.length > 1) {
                idsPart = parts[1];
            } else {
                return new ArrayList<>();
            }
        }
        
        if (idsPart.isEmpty()) return new ArrayList<>();

        return Arrays.stream(idsPart.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    @Override
    public String getQuizTitle(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String data = redisTemplate.opsForValue().get(key);
        if (data == null || data.isEmpty()) {
            return "Dynamic Quiz";
        }
        
        if (data.contains("####")) {
            return data.split("####")[0];
        }
        
        return "Dynamic Quiz"; // Fallback for legacy sessions
    }
}
