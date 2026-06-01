package com.icemberg.QuizApplication.service.impl;

import com.icemberg.QuizApplication.dto.ExplanationResponse;
import com.icemberg.QuizApplication.service.interfaces.QuizExplanationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizExplanationServiceImpl implements QuizExplanationService {

    private static final Logger log = LoggerFactory.getLogger(QuizExplanationServiceImpl.class);

    private final ChatModel chatModel;
    private final RobustSearchService searchService;

    public QuizExplanationServiceImpl(ChatModel chatModel, RobustSearchService searchService) {
        this.chatModel = chatModel;
        this.searchService = searchService;
    }

    @Override
    public ExplanationResponse generateFailureExplanation(String question, String wrongAnswer, String correctAnswer) {

        // Step 1: Always search the web first for factual context
        String searchContext;
        try {
            log.info("Searching web context for question: {}", question);
            searchContext = searchService.searchWebWithFallback(question + " " + correctAnswer);
        } catch (Exception e) {
            log.warn("Web search failed entirely, LLM will use internal knowledge: {}", e.getMessage());
            searchContext = "Web search could not retrieve live data. Please rely on your internal knowledge base.";
        }

        // Step 2: Build the prompt with injected search context
        String systemInstruction =
            "You are an encouraging, highly knowledgeable AI academic tutor.\n" +
            "Your task is to analyze quiz failures using the provided web search context to ensure accuracy.\n" +
            "Format your response using clear Markdown headers.";

        String userPrompt = String.format(
            "A student answered this question incorrectly.\n\n" +
            "**Question:** %s\n" +
            "**Student's Incorrect Answer:** %s\n" +
            "**The True Correct Answer:** %s\n\n" +
            "--- Web Search Context ---\n%s\n--- End Context ---\n\n" +
            "Using the above context, generate a detailed explanation covering:\n" +
            "1. **Why the Correct Answer is Right:** Explain the factual or scientific basis.\n" +
            "2. **Why the Student's Answer is Wrong:** Identify the specific misconception or error.",
            question, wrongAnswer, correctAnswer, searchContext
        );

        // Step 3: Call the Groq LLM via ChatModel
        try {
            log.info("Calling Groq LLM for remedial explanation...");

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemInstruction),
                    new UserMessage(userPrompt)
            ));

            ChatResponse response = chatModel.call(prompt);
            String content = response.getResult().getOutput().getText();

            log.info("Successfully generated remedial explanation.");
            return new ExplanationResponse(content);

        } catch (Exception e) {
            log.error("Failed to generate explanation from LLM: {}", e.getMessage(), e);
            // Ultimate fallback if even the LLM API endpoint drops out
            return new ExplanationResponse(
                "### Explanation Unavailable\n" +
                "We are currently unable to generate a detailed explanation, but the correct answer is: **" +
                correctAnswer + "**."
            );
        }
    }
}
