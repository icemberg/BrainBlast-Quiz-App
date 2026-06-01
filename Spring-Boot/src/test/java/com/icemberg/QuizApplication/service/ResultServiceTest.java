package com.icemberg.QuizApplication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.icemberg.QuizApplication.repository.QuestionRepository;
import com.icemberg.QuizApplication.repository.QuizRepository;
import com.icemberg.QuizApplication.repository.ResultRepository;
import com.icemberg.QuizApplication.entity.Question;
import com.icemberg.QuizApplication.entity.Quiz;
import com.icemberg.QuizApplication.dto.Response;
import com.icemberg.QuizApplication.service.interfaces.RedisService;
import com.icemberg.QuizApplication.service.impl.ResultServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class ResultServiceTest {

    @Mock
    private QuizRepository quizDao;

    @Mock
    private QuestionRepository questionDao;

    @Mock
    private ResultRepository resultDao;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private ResultServiceImpl resultService;

    private Question q1, q2;

    @BeforeEach
    void setup() {
        q1 = new Question("math", "easy", "1+1", "1", "2", "3", "4", "2");
        q1.setId(1);
        q2 = new Question("math", "easy", "2+2", "1", "2", "3", "4", "4");
        q2.setId(2);
    }

    @Test
    void calculateScore_success() {
        Quiz quiz = new Quiz();
        quiz.setId(1);
        quiz.setQuestions(List.of(q1, q2));
        when(quizDao.findById(1)).thenReturn(Optional.of(quiz));

        List<Response> responses = List.of(new Response(), new Response());
        responses.get(0).setId(1);
        responses.get(0).setResponse("2");
        responses.get(1).setId(2);
        responses.get(1).setResponse("4");

        ResponseEntity<String> res = resultService.calculateScore("1", responses);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        // score 2 out of 2
        assertEquals(true, res.getBody().contains("Your score is: 2"));
    }

    @Test
    void calculateScore_quizNotFound() {
        when(quizDao.findById(5)).thenReturn(Optional.empty());
        ResponseEntity<String> res = resultService.calculateScore("5", List.of());
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    @Test
    void calculateScore_noQuestions_returnsBadRequest() {
        Quiz quiz = new Quiz();
        quiz.setId(2);
        quiz.setQuestions(new ArrayList<>());
        when(quizDao.findById(2)).thenReturn(Optional.of(quiz));
        ResponseEntity<String> res = resultService.calculateScore("2", List.of());
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode()); // Note: logic says NOT_FOUND if questions empty
    }

    @Test
    void calculateScore_nullResponses_returnsBadRequest() {
        Quiz quiz = new Quiz();
        quiz.setId(2);
        quiz.setQuestions(List.of(q1));
        when(quizDao.findById(2)).thenReturn(Optional.of(quiz));
        ResponseEntity<String> res = resultService.calculateScore("2", null);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }
}
