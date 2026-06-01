package com.icemberg.QuizApplication.exception;

import com.icemberg.QuizApplication.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles missing resource/route errors by forwarding to the React SPA.
     * This is a safety net: the catch-all controller in SpaForwardingController
     * should handle most SPA routes, but if anything slips through, this ensures
     * the React app gets loaded instead of a JSON error.
     *
     * Because @ExceptionHandler picks the most specific match,
     * this runs BEFORE the generic Exception handler below.
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public Object handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException ex,
            HttpServletRequest request) {
        // If the request is for an API path, return a proper JSON 404
        String uri = request.getRequestURI();
        if (uri.startsWith("/auth/") || uri.startsWith("/quiz/") ||
            uri.startsWith("/question/") || uri.startsWith("/admin/")) {
            ErrorResponse errorDetails = new ErrorResponse(ex.getMessage(), "uri=" + uri);
            return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
        }
        // Otherwise, forward to the React SPA
        return new org.springframework.web.servlet.ModelAndView("forward:/index.html");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
