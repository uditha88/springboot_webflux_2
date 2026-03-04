package com.example.webflux.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TopicMessage {
    @JsonProperty("topic")
    private String topic;

    @JsonProperty("messages")
    private List<String> messages;

    public TopicMessage(String topic, List<String> messages) {
        this.topic = topic;
        this.messages = messages;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
