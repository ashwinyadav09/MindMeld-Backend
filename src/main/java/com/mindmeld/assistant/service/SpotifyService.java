package com.mindmeld.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SpotifyService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    @Value("${spotify.api.token}")
    private String token;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String URL = "https://api.spotify.com/v1/search";

    public SpotifyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.restTemplate.getInterceptors().add((req, body, exec) -> {
            req.getHeaders().add("Authorization", "Bearer " + token);
            return exec.execute(req, body);
        });
    }

    public String getPlaylist(String sentiment) {
        logger.info("Fetching playlist for sentiment: {}", sentiment);
        String query = sentiment.equals("positive") ? "happy playlist" : "calm playlist";
        String uri = UriComponentsBuilder.fromHttpUrl(URL)
                .queryParam("q", query)
                .queryParam("type", "playlist")
                .queryParam("limit", 1)
                .encode()
                .toUriString();
        logger.debug("Spotify Request URL: {}", uri);

        String response = restTemplate.getForObject(uri, String.class);
        if (response == null) {
            logger.error("Spotify API returned null response");
            throw new RuntimeException("Spotify API returned null response");
        }
        logger.info("Spotify Response: {}", response);

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode playlists = root.path("playlists").path("items");
            if (playlists.isEmpty() || !playlists.isArray()) {
                logger.error("No playlists found in response: {}", response);
                throw new RuntimeException("No playlists found in response");
            }
            JsonNode firstPlaylist = playlists.get(0);
            JsonNode externalUrls = firstPlaylist.path("external_urls");
            String playlistUrl = externalUrls.path("spotify").asText();
            if (playlistUrl.isEmpty()) {
                logger.error("No Spotify URL in first playlist: {}", response);
                throw new RuntimeException("No Spotify URL found");
            }
            logger.info("Extracted Playlist URL: {}", playlistUrl);
            return playlistUrl;
        } catch (Exception e) {
            logger.error("Failed to parse Spotify response: {}", response, e);
            throw new RuntimeException("Failed to parse Spotify response", e);
        }
    }
}