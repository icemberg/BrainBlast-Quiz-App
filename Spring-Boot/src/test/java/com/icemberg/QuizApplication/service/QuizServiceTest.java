package com.icemberg.QuizApplication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.icemberg.QuizApplication.repository.QuestionRepository;
import com.icemberg.QuizApplication.repository.QuizRepository;
import com.icemberg.QuizApplication.entity.Question;
import com.icemberg.QuizApplication.entity.Quiz;
import com.icemberg.QuizApplication.dto.QuestionWrapper;
import com.icemberg.QuizApplication.service.interfaces.RedisService;
import com.icemberg.QuizApplication.service.impl.QuizServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {

    @Mock
    private QuizRepository quizDao;

    @Mock
    private QuestionRepository questionDao;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private QuizServiceImpl quizService;

    private Question q1, q2;

    @BeforeEach
    void setup() {
        q1 = new Question("math", "easy", "1+1", "1", "2", "3", "4", "2");
        q1.setId(1);
        q2 = new Question("math", "easy", "2+2", "1", "2", "3", "4", "4");
        q2.setId(2);
    }

    @Test
    void createQuiz_success() {
        List<Question> list = List.of(q1, q2);
        // Mock Redis: No excluded IDs initially
        when(redisService.getSeenQuestionIds(any(), any())).thenReturn(new ArrayList<>());
        when(questionDao.findRandomQuestionsByCategory("math", 2)).thenReturn(list);

        ResponseEntity<?> res = quizService.createQuiz("math", 2, "My Quiz");
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        verify(redisService, times(1)).addSeenQuestionIds(any(), any(), any());
        verify(redisService, times(1)).saveQuizSession(any(), any(), any()); // Verify session save
        assertTrue(res.getBody() instanceof java.util.Map); // Returns Map DTO
    }

    @Test
    void createQuiz_noQuestions_returnsNotFound() {
        when(redisService.getSeenQuestionIds(any(), any())).thenReturn(new ArrayList<>());
        when(questionDao.findRandomQuestionsByCategory("math", 2)).thenReturn(new ArrayList<>());
        
        ResponseEntity<?> res = quizService.createQuiz("math", 2, "My Quiz");
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals("No questions found for category: math", res.getBody());
    }

    @Test
    void getQuizQuestions_success() {
        Quiz quiz = new Quiz();
        quiz.setId(1);
        quiz.setQuestions(List.of(q1, q2));
        when(quizDao.findById(1)).thenReturn(Optional.of(quiz));

        ResponseEntity<List<QuestionWrapper>> res = quizService.getQuizQuestions("1");
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(2, res.getBody().size());
    }

    @Test
    void getQuizQuestions_notFound() {
        when(quizDao.findById(99)).thenReturn(Optional.empty());
        ResponseEntity<List<QuestionWrapper>> res = quizService.getQuizQuestions("99");
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }
}
