package com.icemberg.QuizApplication.config;

import com.icemberg.QuizApplication.repository.UserRepository;
import com.icemberg.QuizApplication.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Autowired
    UserRepository userDao;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            String adminUsername = "admin";
            // Check if admin exists
            User admin = userDao.findByUsername(adminUsername).orElse(null);
            
            if (admin == null) {
                // Create new admin
                User newAdmin = new User();
                newAdmin.setUsername(adminUsername);
                newAdmin.setPassword(passwordEncoder.encode("admin123"));
                newAdmin.setRole("ADMIN");
                userDao.save(newAdmin);
                System.out.println("Admin user created: admin / admin123");
            } else {
                // Update existing admin just in case role was wrong
                if (!"ADMIN".equals(admin.getRole())) {
                    admin.setRole("ADMIN");
                    userDao.save(admin);
                    System.out.println("Existing user 'admin' promoted to ADMIN role.");
                }
            }
        };
    }
}
