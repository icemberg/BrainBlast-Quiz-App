package com.icemberg.QuizApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.icemberg.QuizApplication.entity.User;
import java.util.Optional;
import java.time.LocalDateTime;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    
    long countByLastActiveAfter(LocalDateTime time);
}

