package com.travelassistant.service;

import org.springframework.stereotype.Service;
import com.travelassistant.util.ContextManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptEngineeringService {

    private final ContextManager contextManager;
    private final PromptTemplateService promptTemplateService;
    private static final int MAX_CONTEXT_LENGTH = 200;

    public String generateDestinationRecommendation(String userInput) {
        String prompt = createPrompt("destination-recommendation", userInput);
        return promptTemplateService.formatPrompt(prompt, userInput);
    }

    public String generateLocalAttractions(String userInput) {
        String prompt = createPrompt("local-attractions", userInput);
        return promptTemplateService.formatPrompt(prompt, userInput);
    }

    public String generatePackingSuggestions(String userInput) {
        String prompt = createPrompt("packing-suggestions", userInput);
        return promptTemplateService.formatPrompt(prompt, userInput);
    }

    private String createPrompt(String promptType, String userInput) {
        String context = contextManager.getCurrentContext();
        if (context.length() > MAX_CONTEXT_LENGTH) {
            context = context.substring(0, MAX_CONTEXT_LENGTH);
        }

        String promptTemplate = promptTemplateService.getPromptTemplate(promptType);

        // Replace placeholders in template with actual values

        String destination = extractDestination(userInput);
        if (!destination.isEmpty()) {
            promptTemplate = promptTemplate.replace("{destination}", destination);
        }

        String duration = extractDuration(userInput);
        if (!duration.isEmpty()) {
            promptTemplate = promptTemplate.replace("{duration}", duration);
        }

        String activities = extractActivities(userInput);
        if (!activities.isEmpty()) {
            promptTemplate = promptTemplate.replace("{activities}", activities);
        }

        promptTemplate = promptTemplate.replace("{context}", context);

        return promptTemplate;
    }

    private String extractDestination(String userInput) {
        String[] locationIndicators = { "in", "to", "at", "for" };
        String[] words = userInput.split("\\s+");

        for (int i = 0; i < words.length - 1; i++) {
            for (String indicator : locationIndicators) {
                if (words[i].equalsIgnoreCase(indicator)) {
                    if (i + 1 < words.length) {
                        return words[i + 1].replaceAll("[,.?!]", "");
                    }
                }
            }
        }
        return "";
    }

    private String extractDuration(String userInput) {
        if (userInput.contains("week") || userInput.contains("day") ||
                userInput.contains("month") || userInput.contains("night")) {

            String[] words = userInput.split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                if (isNumeric(words[i]) &&
                        (words[i + 1].contains("day") || words[i + 1].contains("week") ||
                                words[i + 1].contains("month") || words[i + 1].contains("night"))) {
                    return words[i] + " " + words[i + 1];
                }
            }
        }
        return "";
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String extractActivities(String userInput) {
        return "general tourism and " +
                (userInput.contains("hiking") ? "hiking " : "") +
                (userInput.contains("swimming") ? "swimming " : "") +
                (userInput.contains("skiing") ? "skiing " : "") +
                (userInput.contains("tours") ? "tours " : "") +
                (userInput.contains("sightseeing") ? "sightseeing " : "") +
                (userInput.contains("shopping") ? "shopping " : "");
    }

    public String chainOfThoughtPrompt(String userInput) {
        return "Let's think step by step about your travel question:\n\n" +
                "1. Understand what information you're looking for\n" +
                "2. Consider relevant factors like season, budget, interests\n" +
                "3. Provide specific, actionable recommendations\n\n" +
                "Your question: " + userInput;
    }
}