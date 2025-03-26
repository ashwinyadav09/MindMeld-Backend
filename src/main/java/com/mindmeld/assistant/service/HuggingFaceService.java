package com.mindmeld.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HuggingFaceService {
    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceService.class);
    @Value("${huggingface.api.key}")
    private String apiKey;
    @Value("${huggingface.api.url:https://api-inference.huggingface.co/models/distilbert-base-uncased-finetuned-sst-2-english}")
    private String apiUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HuggingFaceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getSentiment(String message) {
        logger.info("Fetching sentiment for message: {}", message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        String requestBody = "{\"inputs\": \"" + message + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String response = restTemplate.postForObject(apiUrl, entity, String.class);
        if (response == null) {
            logger.error("Hugging Face API returned null response");
            throw new RuntimeException("Hugging Face API returned null");
        }
        logger.info("Hugging Face Response: {}", response);

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode sentimentNode = root.get(0).get(0); // First result, highest score
            String label = sentimentNode.path("label").asText().toLowerCase();
            logger.info("Detected Sentiment: {}", label);
            return label; // Returns "positive" or "negative"
        } catch (Exception e) {
            logger.error("Failed to parse Hugging Face response: {}", response, e);
            throw new RuntimeException("Failed to parse Hugging Face response", e);
        }
    }
}