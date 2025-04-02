package com.travelassistant.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Conversation {
    @Getter
    private List<Message> messages;
    @Getter
    private String context;

    private static final int MAX_CONTEXT_LENGTH = 1000;
    private static final int MAX_MESSAGES = 10;
    private static final int SUMMARY_LENGTH = 200;

    public Conversation() {
        this.messages = new ArrayList<>();
        this.context = "";
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        if (this.messages.size() > MAX_MESSAGES) {
            this.messages.remove(0); // Remove oldest message when exceeding limit
        }
        updateContext(message);
    }

    public void addMessage(String sender, String content) {
        Message message = new Message(sender, content);
        addMessage(message);
    }

    public void addContextInfo(String info) {
        if (info == null || info.isEmpty()) {
            return;
        }

        this.context += info + " ";

        if (this.context.length() > MAX_CONTEXT_LENGTH) {
            log.debug("Trimming context from {} characters", this.context.length());
            this.context = this.context.substring(0, MAX_CONTEXT_LENGTH);
            // Make sure we don't cut in the middle of a word
            int lastSpace = this.context.lastIndexOf(" ");
            if (lastSpace > 0) {
                this.context = this.context.substring(0, lastSpace);
            }
            log.debug("Context trimmed to {} characters", this.context.length());
        }
    }

    private void updateContext(Message message) {
        if ("user".equals(message.getSender())) {
            addContextInfo("User asked: " + message.getContent());
        } else if ("assistant".equals(message.getSender())) {
            addContextInfo("Information provided about: " + getSummaryTopic(message.getContent()));
        }
    }

    private String getSummaryTopic(String content) {
        if (content == null || content.length() < 50) {
            return content;
        }

        // Get first chars as a simple summary
        String shortSummary = content.substring(0, Math.min(SUMMARY_LENGTH, content.length()));
        return shortSummary + "...";
    }

    public void clear() {
        this.messages.clear();
        this.context = "";
    }

}