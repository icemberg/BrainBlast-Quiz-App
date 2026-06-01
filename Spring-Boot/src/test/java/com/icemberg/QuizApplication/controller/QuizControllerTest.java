package com.icemberg.QuizApplication.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icemberg.QuizApplication.dto.Response;
import com.icemberg.QuizApplication.service.interfaces.QuizService;
import com.icemberg.QuizApplication.service.interfaces.ResultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(QuizController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class QuizControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private ResultService resultService;

    @MockitoBean
    private com.icemberg.QuizApplication.filter.JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private com.icemberg.QuizApplication.service.impl.CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private com.icemberg.QuizApplication.config.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void createQuiz_ok() throws Exception {
        ResponseEntity<Object> response = new ResponseEntity<>(new com.icemberg.QuizApplication.entity.Quiz(), HttpStatus.CREATED);
        when(quizService.createQuiz(any(String.class), anyInt(), any(String.class))).thenReturn((ResponseEntity) response);
        mvc.perform(post("/quiz/createQuiz?category=math&numQ=2&title=Test")).andExpect(status().isCreated());
    }

    @Test
    void getQuizQuestions_ok() throws Exception {
        when(quizService.getQuizQuestions("1")).thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));
        mvc.perform(get("/quiz/getQuizQuestions/1")).andExpect(status().isOk());
    }

    @Test
    void submitQuiz_ok() throws Exception {
        when(resultService.calculateScore(any(String.class), any())).thenReturn(new ResponseEntity<>("score", HttpStatus.OK));
        List<Response> responses = List.of();
        mvc.perform(post("/quiz/submitQuiz/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(responses))).andExpect(status().isOk());
    }
}
