package com.travelassistant.service;

import com.travelassistant.dto.ConversationRequest;
import com.travelassistant.dto.ConversationResponse;
import com.travelassistant.model.Conversation;
import com.travelassistant.model.Message;
import com.travelassistant.util.ContextManager;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ContextManager contextManager;
    private final GeminiService geminiService;
    private final WeatherService weatherService;
    private final CountryInfoService countryInfoService;
    private final PromptEngineeringService promptEngineeringService;

    public ConversationResponse processConversation(ConversationRequest request) {
        String userId = request.userId();

        Conversation conversation = contextManager.getConversation(userId);

        String userMessage = request.message();
        conversation.addMessage("user", userMessage);

        contextManager.updateConversation(userId, conversation);

        // Determine if we need external data based on message content
        boolean needsWeatherData = needsWeatherInfo(userMessage);
        boolean needsCountryInfo = needsCountryInfo(userMessage);

        if (needsWeatherData || needsCountryInfo) {
            enrichContextWithExternalData(conversation, userMessage, needsWeatherData, needsCountryInfo);
        }

        String relevantContext = getRelevantContext(conversation, userMessage);
        String prompt = determinePromptType(userMessage, relevantContext);

        try {
            log.debug("Sending prompt to LLM, length: {}", prompt.length());
            String responseMessage = geminiService.generateResponse(prompt);

            conversation.addMessage("assistant", responseMessage);

            contextManager.updateConversation(userId, conversation);

            return new ConversationResponse(
                    responseMessage,
                    generateSuggestions(conversation, userMessage),
                    false);
        } catch (Exception e) {
            log.error("Error processing conversation", e);
            return new ConversationResponse(
                    "I'm sorry, I couldn't process your request at the moment.",
                    null,
                    true);
        }
    }

    private String getRelevantContext(Conversation conversation, String userMessage) {
        StringBuilder contextBuilder = new StringBuilder();
        for (Message msg : conversation.getMessages()) {
            contextBuilder.append(msg.getSender())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }
        return contextBuilder.toString();
    }

    private String determinePromptType(String userMessage, String context) {
        String messageLower = userMessage.toLowerCase();
        String fullPrompt;

        // Incorporate the full context into the prompt generation call
        if (messageLower.contains("recommend") ||
                messageLower.contains("where should") ||
                messageLower.contains("best place")) {
            fullPrompt = promptEngineeringService.generateDestinationRecommendation(context + "\nUser: " + userMessage);
        } else if (messageLower.contains("attraction") ||
                messageLower.contains("visit") ||
                messageLower.contains("see in")) {
            fullPrompt = promptEngineeringService.generateLocalAttractions(context + "\nUser: " + userMessage);
        } else if (messageLower.contains("pack") ||
                messageLower.contains("bring") ||
                messageLower.contains("luggage")) {
            fullPrompt = promptEngineeringService.generatePackingSuggestions(context + "\nUser: " + userMessage);
        } else if (messageLower.contains("why") ||
                messageLower.contains("how come") ||
                messageLower.contains("explain")) {
            fullPrompt = promptEngineeringService.chainOfThoughtPrompt(context + "\nUser: " + userMessage);
        } else {
            fullPrompt = "You are a helpful travel assistant. Here is the conversation so far:\n" +
                    context +
                    "\nUser: " + userMessage;
        }
        return fullPrompt;
    }

    private boolean needsWeatherInfo(String message) {
        return message.toLowerCase().contains("weather") ||
                message.toLowerCase().contains("temperature") ||
                message.toLowerCase().contains("rain") ||
                message.toLowerCase().contains("sunny");
    }

    private boolean needsCountryInfo(String message) {
        return message.toLowerCase().contains("country") ||
                message.toLowerCase().contains("capital") ||
                message.toLowerCase().contains("language") ||
                message.toLowerCase().contains("currency");
    }

    private void enrichContextWithExternalData(Conversation conversation, String message,
            boolean needsWeather, boolean needsCountry) {
        // Simple extraction of location - in a real app, use NLP for better extraction
        String[] words = message.split("\\s+");
        String potentialLocation = "";

        for (int i = 0; i < words.length; i++) {
            if (words[i].equalsIgnoreCase("in") || words[i].equalsIgnoreCase("to") ||
                    words[i].equalsIgnoreCase("at")) {
                if (i + 1 < words.length) {
                    potentialLocation = words[i + 1].replaceAll("[,.?!]", "");
                    break;
                }
            }
        }

        if (!potentialLocation.isEmpty()) {
            if (needsWeather) {
                try {
                    var weatherData = weatherService.getCurrentWeather(potentialLocation);
                    conversation.addContextInfo("Current weather in " + potentialLocation + ": " +
                            weatherData.getTemperature() + "°C, " +
                            weatherData.getDescription());
                } catch (Exception e) {
                    // Weather API failure shouldn't stop the conversation
                    conversation.addContextInfo("Note: Weather information is currently unavailable.");
                }
            }

            if (needsCountry) {
                try {
                    var countryData = countryInfoService.getCountryInfo(potentialLocation);
                    conversation.addContextInfo("Country information for " + potentialLocation + ": " +
                            "Capital: " + countryData.getCapital() + ", " +
                            "Currency: " + countryData.getCurrencies() + ", " +
                            "Languages: " + countryData.getLanguages());
                } catch (Exception e) {
                    // Country API failure shouldn't stop the conversation
                    conversation.addContextInfo("Note: Country information is currently unavailable.");
                }
            }
        }
    }

    private ArrayList<String> generateSuggestions(Conversation conversation, String userMessage) {
        ArrayList<String> suggestions = new ArrayList<>();
        String messageLower = userMessage.toLowerCase();

        if (messageLower.contains("destination") ||
                messageLower.contains("where") ||
                messageLower.contains("visit") ||
                messageLower.contains("travel to")) {
            suggestions.add("What's the best time to visit this destination?");
            suggestions.add("What are the must-see attractions there?");
            suggestions.add("How is the local transportation?");
            suggestions.add("What should I know about the local cuisine?");
        }

        else if (messageLower.contains("weather") ||
                messageLower.contains("temperature") ||
                messageLower.contains("climate") ||
                messageLower.contains("season")) {
            suggestions.add("What clothes should I pack for this weather?");
            suggestions.add("When is the best season to visit?");
            suggestions.add("Are there any weather-related events I should be aware of?");
        }

        else if (messageLower.contains("hotel") ||
                messageLower.contains("stay") ||
                messageLower.contains("accommodation") ||
                messageLower.contains("lodging")) {
            suggestions.add("What neighborhoods are best for tourists?");
            suggestions.add("What's the typical price range for accommodations?");
            suggestions.add("Are there any local hospitality customs I should know?");
        }

        else if (messageLower.contains("transport") ||
                messageLower.contains("getting around") ||
                messageLower.contains("travel between") ||
                messageLower.contains("flight") ||
                messageLower.contains("train")) {
            suggestions.add("What's the best way to get around locally?");
            suggestions.add("Is public transportation reliable there?");
            suggestions.add("Should I rent a car or use other transportation?");
        }

        else if (messageLower.contains("food") ||
                messageLower.contains("eat") ||
                messageLower.contains("restaurant") ||
                messageLower.contains("cuisine")) {
            suggestions.add("What are the must-try local dishes?");
            suggestions.add("Are there any dining customs I should know about?");
            suggestions.add("What's a typical meal budget I should plan for?");
        }

        else if (messageLower.contains("culture") ||
                messageLower.contains("tradition") ||
                messageLower.contains("custom") ||
                messageLower.contains("etiquette")) {
            suggestions.add("What are important cultural do's and don'ts?");
            suggestions.add("Are there any festivals or events happening?");
            suggestions.add("How should I dress to be respectful of local customs?");
        }

        else if (messageLower.contains("budget") ||
                messageLower.contains("cost") ||
                messageLower.contains("money") ||
                messageLower.contains("expense") ||
                messageLower.contains("currency")) {
            suggestions.add("What are typical daily expenses for travelers?");
            suggestions.add("Where can I exchange currency at the best rates?");
            suggestions.add("Are credit cards widely accepted?");
        }

        else if (messageLower.contains("safe") ||
                messageLower.contains("security") ||
                messageLower.contains("danger")) {
            suggestions.add("What precautions should travelers take?");
            suggestions.add("Which areas should I avoid?");
            suggestions.add("What emergency numbers should I know?");
        }

        else if (messageLower.contains("language") ||
                messageLower.contains("speak") ||
                messageLower.contains("communicate")) {
            suggestions.add("What are some useful local phrases to know?");
            suggestions.add("How widely is English spoken there?");
            suggestions.add("Are there any translation apps that work well offline?");
        }

        else {
            suggestions.add("What should I know before traveling there?");
            suggestions.add("What's the best time of year to visit?");
            suggestions.add("What are the top attractions?");
            suggestions.add("How's the local cuisine?");
        }

        return suggestions.isEmpty() ? null : suggestions;
    }
}