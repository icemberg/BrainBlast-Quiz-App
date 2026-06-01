package com.icemberg.QuizApplication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;

import com.icemberg.QuizApplication.repository.QuestionRepository;
import com.icemberg.QuizApplication.entity.Question;
import com.icemberg.QuizApplication.service.impl.QuestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionDao;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Question sample;

    @BeforeEach
    void setup() {
        sample = new Question("math", "easy", "1+1", "1", "2", "3", "4", "2");
        sample.setId(1);
    }

    @Test
    void getAllQuestions_returnsList() {
        List<Question> list = List.of(sample);
        when(questionDao.findAll()).thenReturn(list);

        ResponseEntity<List<Question>> res = questionService.getAllQuestions();
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void getQuestionsByCategory_returnsList() {
        List<Question> list = List.of(sample);
        when(questionDao.findByCategory("math")).thenReturn(list);

        ResponseEntity<List<Question>> res = questionService.getQuestionsByCategory("math");
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void addQuestion_savesAndReturnsCreated() {
        when(questionDao.saveAndFlush(any(Question.class))).thenReturn(sample);
        ResponseEntity<String> res = questionService.addQuestion(sample);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    void updateQuestion_existing_updates() {
        when(questionDao.findById(1)).thenReturn(Optional.of(sample));
        ResponseEntity<String> res = questionService.updateQuestion(1, sample);
        assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
        verify(questionDao, times(1)).save(any(Question.class));
    }

    @Test
    void updateQuestion_missing_returnsNotFound() {
        when(questionDao.findById(2)).thenReturn(Optional.empty());
        ResponseEntity<String> res = questionService.updateQuestion(2, sample);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    @Test
    void deleteQuestion_existing_deletes() {
        when(questionDao.existsById(1)).thenReturn(true);
        doNothing().when(questionDao).deleteById(1);
        ResponseEntity<String> res = questionService.deleteQuestion(1);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        verify(questionDao, times(1)).deleteById(1);
    }

    @Test
    void deleteQuestion_missing_returnsNotFound() {
        when(questionDao.existsById(2)).thenReturn(false);
        ResponseEntity<String> res = questionService.deleteQuestion(2);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }
}
