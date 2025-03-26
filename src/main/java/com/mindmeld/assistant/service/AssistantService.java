package com.mindmeld.assistant.service;

import com.mindmeld.assistant.entity.ChatLog;
import com.mindmeld.assistant.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AssistantService {
    private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);
    @Autowired private GeminiService geminiService;
    @Autowired private HuggingFaceService huggingFaceService;
    @Autowired private SpotifyService spotifyService;

    private final Random random = new Random();

    public String processMessage(String message, List<ChatLog> history, User user) {
        logger.info("Processing message: {} for user: {}", message, user.getEmail());

        // Analyze sentiment
        String sentiment = getSentiment(message);
        logger.info("Detected sentiment: {}", sentiment);

        // Build context from history (last 5 messages)
        StringBuilder context = new StringBuilder();
        int historySize = Math.min(history.size(), 5);
        for (int i = history.size() - historySize; i < history.size(); i++) {
            ChatLog log = history.get(i);
            context.append("User: ").append(log.getMessage()).append("\n");
            context.append("Assistant: ").append(log.getResponse()).append("\n");
        }
        context.append("User: ").append(message).append("\n");

        // Generate conversational response
        String prompt = "You are an empathetic support assistant. Based on this conversation:\n" +
                       context + "Respond thoughtfully, ask questions to keep the dialogue going, " +
                       "and only offer suggestions if it feels natural and relevant to the user’s needs.";
        String response = geminiService.getResponse(prompt);
        logger.info("Gemini Response: {}", response);

        // Add AI-generated suggestion if appropriate
        if (shouldSuggest(history.size(), message)) {
            String suggestion = generateSuggestion(sentiment, context.toString(), message);
            response += "\n" + suggestion;
            logger.info("Added Suggestion: {}", suggestion);
        }

        return response;
    }

    private String getSentiment(String message) {
        try {
            return huggingFaceService.getSentiment(message);
        } catch (Exception e) {
            logger.warn("Sentiment analysis failed: {}", e.getMessage());
            return "neutral";
        }
    }

    private boolean shouldSuggest(int turnCount, String message) {
        boolean userNeedsHelp = message.toLowerCase().contains("help") || 
                               message.toLowerCase().contains("what should i do");
        return turnCount > 1 && (userNeedsHelp || turnCount % 3 == 0 || random.nextInt(100) < 30);
    }

    private String generateSuggestion(String sentiment, String context, String message) {
        if (message.toLowerCase().contains("music") || message.toLowerCase().contains("playlist")) {
            try {
                String playlist = spotifyService.getPlaylist(sentiment);
                return "Here’s a " + (sentiment.equals("positive") ? "happy" : "calming") + 
                       " playlist for you: " + playlist;
            } catch (Exception e) {
                logger.warn("Spotify failed: {}", e.getMessage());
                return "I tried to find a playlist, but it didn’t work—any other ideas you’d like to explore?";
            }
        }

        String suggestionPrompt = "You are an empathetic support assistant. Given this conversation:\n" +
                                 context + "and knowing the user’s sentiment is " + sentiment + ", " +
                                 "provide a single, thoughtful suggestion that feels natural and relevant " +
                                 "to the user’s current message: '" + message + "'. Keep it concise.";
        return geminiService.getResponse(suggestionPrompt);
    }
}