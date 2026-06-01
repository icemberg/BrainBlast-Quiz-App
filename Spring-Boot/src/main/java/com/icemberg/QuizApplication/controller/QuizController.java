package com.icemberg.QuizApplication.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.icemberg.QuizApplication.dto.QuestionWrapper;
import com.icemberg.QuizApplication.dto.QuizCorrectionRequest;
import com.icemberg.QuizApplication.dto.ExplanationResponse;
import com.icemberg.QuizApplication.dto.AnswerVerificationRequest;
import com.icemberg.QuizApplication.dto.AnswerVerificationResponse;
import com.icemberg.QuizApplication.entity.Quiz;
import com.icemberg.QuizApplication.dto.Response;
import com.icemberg.QuizApplication.service.interfaces.QuizService;
import com.icemberg.QuizApplication.service.interfaces.QuizExplanationService;
import com.icemberg.QuizApplication.service.interfaces.ResultService;

@RestController
@RequestMapping("quiz")
public class QuizController {
    
    @Autowired
    QuizService quizService;

    @Autowired
    ResultService resultService;

    @Autowired
    QuizExplanationService explanationService;

    @PostMapping("/createQuiz")
    public ResponseEntity<?> createQuiz(@RequestParam String category, @RequestParam int numQ, @RequestParam String title) {
        return quizService.createQuiz(category, numQ, title);
    }

    @GetMapping("/getQuizQuestions/{id}")
    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(@PathVariable String id) {
        return quizService.getQuizQuestions(id);
    }

    @GetMapping("/allQuizzes")
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    @GetMapping("/count/{category}")
    public ResponseEntity<Long> getQuestionCount(@PathVariable String category) {
        return quizService.getQuestionCount(category);
    }

    @PostMapping("/submitQuiz/{id}")
    public ResponseEntity<String> submitQuiz(@PathVariable String id, @RequestBody List<Response> responses) {
        return resultService.calculateScore(id, responses);
    }

    @PostMapping("/start")
    public ResponseEntity<?> startDynamicQuiz(@RequestParam String category, @RequestParam int numQ, @RequestParam String title) {
        return quizService.createQuiz(category, numQ, title);
    }
    
    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupQuizzes() {
        String result = quizService.performCleanup();
        return new ResponseEntity<>(result, org.springframework.http.HttpStatus.OK);
    }

    @PostMapping("/verifyAnswer")
    public ResponseEntity<AnswerVerificationResponse> verifyAnswer(@RequestBody AnswerVerificationRequest request) {
        AnswerVerificationResponse verification = resultService.verifyAnswer(
            request.getQuestionId(),
            request.getSelectedAnswer()
        );
        return ResponseEntity.ok(verification);
    }

    @PostMapping("/remedial-explanation")
    public ResponseEntity<ExplanationResponse> handleIncorrectAnswer(@RequestBody QuizCorrectionRequest request) {
        ExplanationResponse feedback = explanationService.generateFailureExplanation(
            request.getQuestion(),
            request.getUserAnswer(),
            request.getCorrectAnswer()
        );
        return ResponseEntity.ok(feedback);
    }
}
