package com.mindmeld.assistant.controller;

import com.mindmeld.assistant.dto.ChatRequest;
import com.mindmeld.assistant.entity.ChatLog;
import com.mindmeld.assistant.entity.User;
import com.mindmeld.assistant.repository.ChatLogRepository;
import com.mindmeld.assistant.repository.UserRepository;
import com.mindmeld.assistant.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired private ChatLogRepository chatLogRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private GeminiService geminiService;

    @PostMapping("/message")
    public String handleMessage(@RequestBody ChatRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Email: " + email);
        User user = userRepo.findByEmail(email);
        if (user == null) return "User not found";
        String message = request.message();
        System.out.println("Message: " + message);
        String response = geminiService.getResponse(message);
        ChatLog log = new ChatLog(user, message, "test");
        log.setResponse(response);
        chatLogRepo.save(log);
        return response;
    }
}