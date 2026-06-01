package com.icemberg.QuizApplication.service.impl;

import com.icemberg.QuizApplication.dto.Response;
import com.icemberg.QuizApplication.entity.Question;
import com.icemberg.QuizApplication.entity.Quiz;
import com.icemberg.QuizApplication.entity.Result;
import com.icemberg.QuizApplication.repository.QuestionRepository;
import com.icemberg.QuizApplication.repository.QuizRepository;
import com.icemberg.QuizApplication.repository.ResultRepository;
import com.icemberg.QuizApplication.service.interfaces.RedisService;
import com.icemberg.QuizApplication.service.interfaces.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ResultServiceImpl implements ResultService {

    @Autowired
    ResultRepository resultDao;

    @Autowired
    QuizRepository quizDao;

    @Autowired
    QuestionRepository questionDao;

    @Autowired
    RedisService redisService;

    @Override
    public ResponseEntity<String> calculateScore(String id, List<Response> responses) {
        try {
            List<Question> questionsFromDB = null;
            String quizTitle = "Dynamic Quiz";

            if (isInteger(id)) {
                Optional<Quiz> quiz = quizDao.findById(Integer.parseInt(id));
                if (quiz.isPresent()) {
                    questionsFromDB = quiz.get().getQuestions();
                    quizTitle = quiz.get().getTitle();
                }
            } else {
                List<Integer> qIds = redisService.getQuizSession(id);
                if (!qIds.isEmpty()) {
                    questionsFromDB = questionDao.findAllById(qIds);
                    quizTitle = redisService.getQuizTitle(id);
                }
            }

            if (questionsFromDB == null || questionsFromDB.isEmpty()) {
                return new ResponseEntity<>("Quiz session not found or expired", HttpStatus.NOT_FOUND);
            }

            if (responses == null) {
                return new ResponseEntity<>("No responses provided", HttpStatus.BAD_REQUEST);
            }

            int score = 0;
            java.util.Map<Integer, String> commandMap = questionsFromDB.stream()
                .collect(java.util.stream.Collectors.toMap(Question::getId, Question::getRightAnswer));

            for (Response r : responses) {
                String correct = commandMap.get(r.getId());
                if (correct != null && correct.equals(r.getResponse())) {
                    score++;
                }
            }
            
            // Save Result
            try {
                String username = "anonymous";
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) 
                    username = auth.getName();

                Result result = new Result();
                result.setUsername(username);
                result.setQuizTitle(quizTitle);
                result.setScore(score);
                result.setTotalQuestions(questionsFromDB.size());
                result.setSubmissionDate(java.time.LocalDateTime.now());
                resultDao.save(result);
            } catch (Exception e) {
               // Ignore stat save failure
            }

            return new ResponseEntity<>("Your score is: " + score + " out of " + questionsFromDB.size(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error calculating score: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isInteger(String s) {
        try { 
            Integer.parseInt(s); 
            return true; 
        } catch(NumberFormatException e) { 
            return false; 
        }
    }
}
