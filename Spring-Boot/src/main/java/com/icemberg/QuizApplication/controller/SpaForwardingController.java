package com.icemberg.QuizApplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Catch-all controller that forwards unmatched browser requests to index.html
 * so that React Router can handle client-side routing.
 *
 * How it works:
 *  - Explicit @RestController mappings (/auth/**, /quiz/**, /question/**, /admin/**)
 *    always take priority over these pattern-matched routes.
 *  - Spring Security filters handle /oauth2/authorization/** and /login/oauth2/code/**
 *    at the filter level, before any controller is invoked.
 *  - The regex [^\\.]*  ensures static asset requests (e.g. main.js, style.css)
 *    are NOT matched and are served normally by the resource handler.
 */
@Controller
public class SpaForwardingController {

    // Single-segment: /login, /register, /home, /dashboard
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String forwardSingleSegment() {
        return "forward:/index.html";
    }

    // Two-segment: /oauth2/redirect, /quiz/{id}, /dashboard/questions
    @RequestMapping(value = "/{path1:[^\\.]*}/{path2:[^\\.]*}")
    public String forwardTwoSegments() {
        return "forward:/index.html";
    }

    // Three-segment: future-proofing for deeper nested routes
    @RequestMapping(value = "/{path1:[^\\.]*}/{path2:[^\\.]*}/{path3:[^\\.]*}")
    public String forwardThreeSegments() {
        return "forward:/index.html";
    }
}