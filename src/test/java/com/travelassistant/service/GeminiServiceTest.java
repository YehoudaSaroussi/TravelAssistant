package com.travelassistant.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.travelassistant.config.GeminiConfig;

public class GeminiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GeminiConfig geminiConfig;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GeminiService geminiService;

    private ObjectMapper realObjectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(geminiConfig.getApiUrl()).thenReturn("https://generativelanguage.googleapis.com");
        when(geminiConfig.getApiKey()).thenReturn("test-key");
        when(geminiConfig.getModelName()).thenReturn("gemini-1.5-flash");

        when(objectMapper.createObjectNode()).thenAnswer(invocation -> realObjectMapper.createObjectNode());
        when(objectMapper.createArrayNode()).thenAnswer(invocation -> realObjectMapper.createArrayNode());
    }

    @Test
    void testGenerateResponse() throws Exception {
        String prompt = "What should I pack for Paris?";
        String expectedResponse = "You should pack comfortable walking shoes, light clothing, and an umbrella.";

        ObjectNode responseJson = createMockResponseJson(expectedResponse);
        String expectedResponseJson = responseJson.toString();

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        when(objectMapper.readTree(anyString())).thenReturn(responseJson);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(expectedResponseJson, HttpStatus.OK);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponseJson);

        String actualResponse = geminiService.generateResponse(prompt);

        assertEquals(expectedResponse, actualResponse);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForObject(urlCaptor.capture(), any(), eq(String.class));
        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.contains("/v1/models/gemini-1.5-flash:generateContent"));
    }

    @Test
    void testErrorHandling() throws Exception {
        String prompt = "What should I pack for Paris?";

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("API Error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            geminiService.generateResponse(prompt);
        });

        assertTrue(exception.getMessage().contains("Failed to generate response"));
    }

    private ObjectNode createMockResponseJson(String responseText) {
        ObjectNode responseJson = realObjectMapper.createObjectNode();
        ObjectNode candidateNode = realObjectMapper.createObjectNode();
        ObjectNode contentNode = realObjectMapper.createObjectNode();
        ObjectNode partNode = realObjectMapper.createObjectNode();

        partNode.put("text", responseText);
        contentNode.set("parts", realObjectMapper.createArrayNode().add(partNode));
        contentNode.put("role", "model");
        candidateNode.set("content", contentNode);
        responseJson.set("candidates", realObjectMapper.createArrayNode().add(candidateNode));

        return responseJson;
    }
}
