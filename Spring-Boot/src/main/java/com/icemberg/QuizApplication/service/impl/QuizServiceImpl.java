package com.icemberg.QuizApplication.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.icemberg.QuizApplication.repository.QuestionRepository;
import com.icemberg.QuizApplication.repository.QuizRepository;
import com.icemberg.QuizApplication.entity.Question;
import com.icemberg.QuizApplication.dto.QuestionWrapper;
import com.icemberg.QuizApplication.entity.Quiz;
import com.icemberg.QuizApplication.service.interfaces.QuizService;
import com.icemberg.QuizApplication.service.interfaces.RedisService;

@Service
@Transactional
public class QuizServiceImpl implements QuizService {

    @Autowired
    QuizRepository quizDao;

    @Autowired
    QuestionRepository questionDao;

    @Autowired
    RedisService redisService;

    @Override
    public ResponseEntity<?> createQuiz(String category, int numQ, String title) {
        try {
            String username = "anonymous";
            try {
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                    username = auth.getName();
                }
            } catch (Exception ignored) { }

            // 1. Get Excluded IDs from Redis
            List<Integer> excludedIds = redisService.getSeenQuestionIds(username, category);
            
            // 2. Fetch Questions (Attempt with exclusions)
            List<Question> questions;
            if (excludedIds.isEmpty()) {
                 questions = questionDao.findRandomQuestionsByCategory(category, numQ);
            } else {
                 questions = questionDao.findRandomQuestionsByCategoryAndExcludeIds(category, numQ, excludedIds);
            }

            // 3. Fallback: If not enough unique questions found, maybe pool exhausted. 
            // Strategy: Clear history for this category and try full random again (Endless Loop)
            if (questions.size() < numQ) {
                // Clear Redis history for this user/category to "reset" the endless mode
                redisService.clearSeenQuestions(username, category);
                // Retry full fetch
                questions = questionDao.findRandomQuestionsByCategory(category, numQ);
            }

            if (questions == null || questions.isEmpty()) {
                return new ResponseEntity<>("No questions found for category: " + category, HttpStatus.NOT_FOUND);
            }
            
            // 4. Update Redis with new selected IDs
            List<Integer> selectedIds = questions.stream().map(Question::getId).toList();
            redisService.addSeenQuestionIds(username, category, selectedIds);
            
            // 5. Create STATELESS Session (Redis) instead of DB Entity
            String sessionId = java.util.UUID.randomUUID().toString();
            redisService.saveQuizSession(sessionId, selectedIds, title);
            
            // Return DTO mimicking Quiz object for Frontend
            // Frontend expects { id: "...", title: "...", category: "...", questions: [...] }
            java.util.Map<String, Object> quizDto = new java.util.HashMap<>();
            quizDto.put("id", sessionId); // String UUID
            quizDto.put("title", title);
            quizDto.put("category", category);
            quizDto.put("questions", questions); 
            
            return new ResponseEntity<>(quizDto, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error creating quiz: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(String id) {
        try {
            List<Question> questionsFromDB = null;
            
            // Check if ID is Integer (Template) or UUID (Session)
            if (isInteger(id)) {
                Optional<Quiz> quiz = quizDao.findById(Integer.parseInt(id));
                if (quiz.isPresent()) questionsFromDB = quiz.get().getQuestions();
            } else {
                // Redis Session
                List<Integer> qIds = redisService.getQuizSession(id);
                if (!qIds.isEmpty()) {
                    questionsFromDB = questionDao.findAllById(qIds);
                }
            }

            if (questionsFromDB == null || questionsFromDB.isEmpty()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }

            List<QuestionWrapper> questionsForUser = new ArrayList<>();
            for (Question q : questionsFromDB) {
                QuestionWrapper qw = new QuestionWrapper(q.getId(), q.getQuestionTitle(), q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4());
                questionsForUser.add(qw);
            }
            return new ResponseEntity<>(questionsForUser, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        try {
            // Only return Template quizzes to the public list (Home Page)
            return new ResponseEntity<>(quizDao.findByIsTemplateTrue(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Long> getQuestionCount(String category) {
        try {
            long count = quizDao.countQuestionsByQuizCategory(category);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public String performCleanup() {
        try {
            List<Quiz> allQuizzes = quizDao.findAll();
            if (allQuizzes.isEmpty()) return "No quizzes to clean.";

            // Group by Category
            java.util.Map<String, List<Quiz>> quizzesByCategory = allQuizzes.stream()
                .collect(java.util.stream.Collectors.groupingBy(Quiz::getCategory));

            int preserved = 0;
            int deleted = 0;

            for (java.util.Map.Entry<String, List<Quiz>> entry : quizzesByCategory.entrySet()) {
                List<Quiz> categoryQuizzes = entry.getValue();
                
                // Identify the "Best" candidate for Template
                // Heuristic: Prefer existing isTemplate=true, else Shortest Title, else Oldest (ID)
                Quiz template = categoryQuizzes.stream()
                    .filter(Quiz::isTemplate)
                    .findFirst()
                    .orElse(null);

                if (template == null) {
                    template = categoryQuizzes.stream()
                        .sorted(java.util.Comparator.comparingInt((Quiz q) -> q.getTitle().length())
                            .thenComparingInt(Quiz::getId))
                        .findFirst()
                        .orElse(null);
                    
                    if (template != null) {
                        template.setTemplate(true);
                        quizDao.save(template);
                    }
                }

                if (template != null) {
                    preserved++;
                    // Delete others in this category
                    for (Quiz q : categoryQuizzes) {
                        if (q.getId() != template.getId()) {
                            quizDao.delete(q);
                            deleted++;
                        }
                    }
                }
            }
            return "Cleanup Complete. Preserved: " + preserved + " templates. Deleted: " + deleted + " duplicates.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Cleanup Failed: " + e.getMessage();
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
