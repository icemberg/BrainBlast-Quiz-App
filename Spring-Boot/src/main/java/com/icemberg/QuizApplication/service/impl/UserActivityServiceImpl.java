package com.icemberg.QuizApplication.service.impl;

import com.icemberg.QuizApplication.repository.UserRepository;
import com.icemberg.QuizApplication.entity.User;
import com.icemberg.QuizApplication.service.interfaces.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserActivityServiceImpl implements UserActivityService {

    @Autowired
    private UserRepository userDao;

    // In-memory cache to throttle DB updates: value is the last time we UPDATED the DB
    private final ConcurrentHashMap<String, LocalDateTime> lastUpdateCache = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void recordActivity(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdate = lastUpdateCache.get(username);

        // Only update DB if it's been more than 5 minutes since last DB write for this user
        if (lastUpdate == null || now.isAfter(lastUpdate.plusMinutes(2))) {
            userDao.findByUsername(username).ifPresent(user -> {
                user.setLastActive(now);
                userDao.save(user);
                lastUpdateCache.put(username, now);
            });
        }
    }
}
