package com.icemberg.QuizApplication.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.icemberg.QuizApplication.repository.QuestionRepository;
import com.icemberg.QuizApplication.entity.Question;
import com.icemberg.QuizApplication.service.interfaces.QuestionService;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Autowired
    QuestionRepository questionDao;

    @Override
    public ResponseEntity<List<Question>> getAllQuestions() {
        try {
            return new ResponseEntity<>(questionDao.findAll(), HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<Question>> getQuestionsByCategory(String category) {
        try {
            return new ResponseEntity<>(questionDao.findByCategory(category),HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        
    }

    @Override
    public ResponseEntity<String> addQuestion(Question question) {
        questionDao.saveAndFlush(question);
        return new ResponseEntity<>("Question added successfully", HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> updateQuestion(Integer id, Question question) {
        Optional<Question> existing = questionDao.findById(id);
        if (existing.isEmpty()) {
            return new ResponseEntity<>("Question with id " + id + " not found", HttpStatus.NOT_FOUND);
        }
        Question q = existing.get();
        q.setCategory(question.getCategory());
        q.setDifficultylevel(question.getDifficultylevel());
        q.setQuestionTitle(question.getQuestionTitle());
        q.setOption1(question.getOption1());
        q.setOption2(question.getOption2());
        q.setOption3(question.getOption3());
        q.setOption4(question.getOption4());
        q.setRightAnswer(question.getRightAnswer());
        questionDao.save(q);
        return new ResponseEntity<>("Question updated successfully", HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<String> deleteQuestion(Integer id) {
        if (!questionDao.existsById(id)) {
            return new ResponseEntity<>("Question with id " + id + " not found", HttpStatus.NOT_FOUND);
        }
        questionDao.deleteById(id);
        return new ResponseEntity<>("Question deleted successfully", HttpStatus.OK);
    }
    
}
