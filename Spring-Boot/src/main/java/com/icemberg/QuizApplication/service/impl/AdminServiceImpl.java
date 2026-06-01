package com.icemberg.QuizApplication.service.impl;

import com.icemberg.QuizApplication.repository.QuizRepository;
import com.icemberg.QuizApplication.repository.ResultRepository;
import com.icemberg.QuizApplication.repository.UserRepository;
import com.icemberg.QuizApplication.service.interfaces.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    QuizRepository quizDao;

    @Autowired
    UserRepository userDao;

    @Autowired
    ResultRepository resultDao;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalQuizzes = quizDao.count();
        long totalUsers = userDao.count();
        long completedQuizzes = resultDao.count();
        Double avgScore = resultDao.findAverageScorePercentage();

        stats.put("totalQuizzes", totalQuizzes);

        // Count users active in the last 5 minutes (Real-time feel)
        long activeNow = userDao.countByLastActiveAfter(java.time.LocalDateTime.now().minusMinutes(2));
        stats.put("activeUsers", activeNow);

        stats.put("completed", completedQuizzes);
        stats.put("avgScore", avgScore != null ? Math.round(avgScore) : 0);
        stats.put("recentActivity", resultDao.findTop5ByOrderBySubmissionDateDesc());

        return stats;
    }
}
