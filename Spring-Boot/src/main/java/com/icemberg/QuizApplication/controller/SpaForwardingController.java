package com.icemberg.QuizApplication.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardingController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // Forward all unmapped (404) requests to the React application
        return "forward:/index.html";
    }
}
