package com.icemberg.QuizApplication.controller;

import com.icemberg.QuizApplication.repository.UserRepository;
import com.icemberg.QuizApplication.dto.AuthRequest;
import com.icemberg.QuizApplication.entity.User;
import com.icemberg.QuizApplication.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin // Allow frontend to call
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Default role if not provided
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        userDao.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        if (authentication.isAuthenticated()) {
            String token = jwtUtil.generateToken(authRequest.getUsername());
            User user = userDao.findByUsername(authRequest.getUsername()).orElse(null);
            
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("token", token);
            response.put("role", user != null ? user.getRole() : "USER");
            response.put("username", authRequest.getUsername());
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnect() {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            userDao.findByUsername(username).ifPresent(user -> {
                user.setLastActive(null); 
                userDao.save(user);
            });
            return ResponseEntity.ok("Disconnected");
        } catch (Exception e) {
             return ResponseEntity.ok("Ignored");
        }
    }


}
