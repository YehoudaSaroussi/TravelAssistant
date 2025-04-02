package com.travelassistant.dto;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {
    private String responseMessage;
    private List<String> suggestions;
    private boolean isError;
}