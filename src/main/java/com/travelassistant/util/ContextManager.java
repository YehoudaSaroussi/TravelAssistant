package com.travelassistant.util;

import com.travelassistant.model.Conversation;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ContextManager {

    private final Map<String, Conversation> conversationContext;
    private String currentContext = "";
    private static final int MAX_CURRENT_CONTEXT_LENGTH = 500;

    public ContextManager() {
        this.conversationContext = new HashMap<>();
    }

    // public void startNewConversation(String userId) {
    // conversationContext.put(userId, new Conversation());
    // this.currentContext = "";
    // }

    public Conversation getConversation(String userId) {
        return conversationContext.computeIfAbsent(userId, k -> new Conversation());
    }

    public void updateConversation(String userId, Conversation conversation) {
        conversationContext.put(userId, conversation);
        this.currentContext = trimContext(conversation.getContext());
        log.debug("Current context updated, length: {}", this.currentContext.length());
    }

    private String trimContext(String context) {
        if (context == null || context.length() <= MAX_CURRENT_CONTEXT_LENGTH) {
            return context;
        }

        return context.substring(0, MAX_CURRENT_CONTEXT_LENGTH);
    }

    public String getContext(Conversation conversation) {
        if (conversation == null) {
            return "";
        }

        String context = conversation.getContext();
        if (context.length() > MAX_CURRENT_CONTEXT_LENGTH) {
            return trimContext(context);
        }
        return context;
    }

    public String getCurrentContext() {
        return this.currentContext;
    }

    public void endConversation(String userId) {
        conversationContext.remove(userId);
    }

    public boolean hasActiveConversation(String userId) {
        return conversationContext.containsKey(userId);
    }

    // Clear all contexts (for testing or reset purposes)
    public void clearAllContexts() {
        conversationContext.clear();
        this.currentContext = "";
    }
}