package com.icemberg.QuizApplication.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icemberg.QuizApplication.entity.Question;
import com.icemberg.QuizApplication.service.interfaces.QuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(QuestionController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class QuestionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private com.icemberg.QuizApplication.filter.JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private com.icemberg.QuizApplication.service.impl.CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private com.icemberg.QuizApplication.config.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllQuestions_ok() throws Exception {
        when(questionService.getAllQuestions()).thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));
        mvc.perform(get("/question/allQuestions")).andExpect(status().isOk());
    }

    @Test
    void addQuestion_ok() throws Exception {
        Question q = new Question("math", "easy", "1+1", "1", "2", "3", "4", "2");
        when(questionService.addQuestion(any(Question.class))).thenReturn(new ResponseEntity<>("Question added", HttpStatus.CREATED));
        mvc.perform(post("/question/addQuestion").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(q))).andExpect(status().isCreated());
    }

    @Test
    void updateQuestion_ok() throws Exception {
        Question q = new Question();
        when(questionService.updateQuestion(any(Integer.class), any(Question.class))).thenReturn(new ResponseEntity<>("updated", HttpStatus.ACCEPTED));
        mvc.perform(put("/question/updateQuestion/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(q))).andExpect(status().isAccepted());
    }

    @Test
    void deleteQuestion_ok() throws Exception {
        when(questionService.deleteQuestion(1)).thenReturn(new ResponseEntity<>("deleted", HttpStatus.OK));
        mvc.perform(delete("/question/deleteQuestion/1")).andExpect(status().isOk());
    }
}
