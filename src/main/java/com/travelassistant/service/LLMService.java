package com.travelassistant.service;

import reactor.core.publisher.Mono;

public interface LLMService {
    Mono<String> generateResponse(String input, String prompt);
}