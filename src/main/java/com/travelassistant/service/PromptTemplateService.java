package com.travelassistant.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PromptTemplateService {

    private static final int MAX_PROMPT_LENGTH = 1500;
    private static final int MAX_TEMPLATE_SIZE = 100;

    public String getPromptTemplate(String promptType) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/" + promptType + ".txt");
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load prompt template: {}", promptType, e);
            return "Could not load prompt template.";
        }
    }

    public String formatPrompt(String template, String userInput) {
        if (template.length() + userInput.length() > MAX_PROMPT_LENGTH) {
            int allowedTemplateLength = MAX_PROMPT_LENGTH - userInput.length();
            if (allowedTemplateLength > MAX_TEMPLATE_SIZE) {
                template = template.substring(0, allowedTemplateLength) + "...";
            }
        }

        String finalPrompt = "You are a helpful travel assistant.\n" +
                template + "\n" +
                "User question: " + userInput;

        log.debug("Generated prompt length: {}", finalPrompt.length());
        return finalPrompt;
    }

    // Keep the original method for backward compatibility
    // public String formatPrompt(String template, Object... args) {
    // return String.format(template, args);
    // }
}