package com.travelassistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.travelassistant.config.GeminiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class GeminiService {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;

    public GeminiService(RestTemplate restTemplate, GeminiConfig geminiConfig, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
        this.objectMapper = objectMapper;
    }

    public String generateResponse(String prompt) {
        try {
            String fullUrl = String.format("%s/v1/models/%s:generateContent?key=%s",
                    geminiConfig.getApiUrl(),
                    geminiConfig.getModelName(),
                    geminiConfig.getApiKey());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode requestBody = objectMapper.createObjectNode();

            ArrayNode contents = objectMapper.createArrayNode();
            ObjectNode content = objectMapper.createObjectNode();

            ArrayNode parts = objectMapper.createArrayNode();
            ObjectNode part = objectMapper.createObjectNode();
            part.put("text", prompt);
            parts.add(part);

            content.set("parts", parts);
            contents.add(content);

            requestBody.set("contents", contents);

            ObjectNode generationConfig = objectMapper.createObjectNode();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.set("generationConfig", generationConfig);

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            log.debug("Sending request to Gemini API: {}", fullUrl);

            String responseBody = restTemplate.postForObject(fullUrl, request, String.class);
            return parseResponse(responseBody);
        } catch (Exception e) {
            log.error("Error generating response from Gemini API", e);
            throw new RuntimeException("Failed to generate response from AI model", e);
        }
    }

    private String parseResponse(String responseJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseJson);

            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

            log.warn("Unable to extract text from Gemini response: {}", responseJson);
            return "Sorry, I couldn't generate a proper response.";
        } catch (Exception e) {
            log.error("Error parsing Gemini API response", e);
            return "Error processing the response.";
        }
    }
}
