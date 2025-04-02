package com.travelassistant.dto;

import jakarta.validation.constraints.NotBlank;

public record ConversationRequest(
                @NotBlank String userId,
                @NotBlank String message,
                String context) {
}