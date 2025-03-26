package com.mindmeld.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    @Value("${gemini.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate(); // Fresh instance
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    public String getResponse(String message) {
        logger.info("Using API Key: {}", apiKey);
        System.out.println("Using API Key: " + apiKey); // Fallback
        String url = URL + apiKey;
        String prompt = "Respond empathetically as a support assistant to: '" + message + "'";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.remove(HttpHeaders.AUTHORIZATION); // Explicitly ensure no Authorization header
        String requestBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + prompt + "\"}]}]}";
        logger.debug("Request URL: {}", url);
        logger.debug("Request Headers: {}", headers);
        logger.debug("Request Body: {}", requestBody);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            logger.info("Response Status: {}", response.getStatusCode());
            System.out.println("Response Status: " + response.getStatusCode()); // Fallback
            String responseBody = response.getBody();
            if (responseBody == null) {
                logger.error("Gemini API returned null response");
                return "Gemini API returned null response";
            }
            logger.info("Gemini Full Response: {}", responseBody);
            System.out.println("Gemini Full Response: " + responseBody); // Fallback

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || candidates.size() == 0) {
                logger.error("No 'candidates' in response: {}", responseBody);
                return "No valid response from Gemini";
            }
            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");
            JsonNode textNode = parts.get(0).path("text");
            String text = textNode.asText();
            logger.info("Extracted Text: {}", text);
            System.out.println("Extracted Text: " + text); // Fallback
            return text;
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.error("Unauthorized (401) from Gemini API: Status={}, Response={}", e.getStatusCode(), e.getResponseBodyAsString());
            System.out.println("Unauthorized (401): " + e.getResponseBodyAsString()); // Fallback
            return "Authentication failed with Gemini API: " + e.getResponseBodyAsString();
        } catch (Exception e) {
            logger.error("Error calling Gemini API: {}", e.getMessage(), e);
            System.out.println("Error: " + e.getMessage()); // Fallback
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
}