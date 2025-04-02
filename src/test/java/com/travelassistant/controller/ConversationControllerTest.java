package com.travelassistant.controller;

import com.travelassistant.dto.ConversationRequest;
import com.travelassistant.dto.ConversationResponse;
import com.travelassistant.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConversationControllerTest {

    @InjectMocks
    private ConversationController conversationController;

    @Mock
    private ConversationService conversationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHandleConversation() {
        ConversationRequest request = new ConversationRequest(
                "user123",
                "What should I pack for my trip?",
                null);

        List<String> suggestions = Arrays.asList("Don't forget your passport!", "Check the weather forecast");

        ConversationResponse mockResponse = new ConversationResponse(
                "You should pack clothes, toiletries, and travel documents.",
                suggestions,
                false);

        when(conversationService.processConversation(any(ConversationRequest.class))).thenReturn(mockResponse);

        ResponseEntity<ConversationResponse> result = conversationController.handleConversation(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mockResponse, result.getBody());

        verify(conversationService).processConversation(request);
    }

    @Test
    public void testHandleError() {
        ConversationRequest request = new ConversationRequest(
                "user123",
                "What about the weather?",
                null);

        when(conversationService.processConversation(any(ConversationRequest.class)))
                .thenThrow(new RuntimeException("Test exception"));

        ResponseEntity<ConversationResponse> result = conversationController.handleConversation(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(true, result.getBody().isError());
        assertEquals("An unexpected error occurred", result.getBody().getResponseMessage());
    }
}