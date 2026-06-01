package com.icemberg.QuizApplication.dto;

import lombok.Data;

@Data
public class Response {
    private Integer id;
    private String response;

    public Integer getId() {
        return id;
    }

    public String getResponse() {
        return response;
    }
}
