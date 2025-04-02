package com.travelassistant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.travelassistant.dto.ConversationRequest;
import com.travelassistant.dto.ConversationResponse;
import com.travelassistant.service.ConversationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ConversationResponse> handleConversation(@Valid @RequestBody ConversationRequest request) {
        log.info("Received conversation request: {}", request);
        try {
            ConversationResponse response = conversationService.processConversation(request);
            log.info("Generated response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing conversation", e);
            ConversationResponse errorResponse = new ConversationResponse(
                    "An unexpected error occurred", null, true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}