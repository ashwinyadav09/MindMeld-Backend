package com.mindmeld.assistant.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import java.time.LocalDateTime;

@Entity(name = "chat_logs")
@Data
public class ChatLog {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private String message;
    private String response;
    private String sentiment;
    private LocalDateTime timestamp;

    public ChatLog() {}
    public ChatLog(User user, String message, String sentiment) {
        this.user = user;
        this.message = message;
        this.sentiment = sentiment;
        this.timestamp = LocalDateTime.now();
    }
}
