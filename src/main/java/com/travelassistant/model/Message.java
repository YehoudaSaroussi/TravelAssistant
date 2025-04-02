package com.travelassistant.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Message {
    private String sender;
    private String content;
    private long timestamp = System.currentTimeMillis();

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }
}