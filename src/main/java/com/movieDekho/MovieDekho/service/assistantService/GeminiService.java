package com.movieDekho.MovieDekho.service.assistantService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieDekho.MovieDekho.config.reqconfig.GeminiConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for calling Google Gemini 2.5 Flash API.
 * 
 * Formats requests according to Gemini's REST API schema:
 * https://ai.google.dev/api/rest/v1beta/models/generateContent
 */
@Service
@Slf4j
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService(GeminiConfig geminiConfig, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.geminiConfig = geminiConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a query to Gemini with system prompt and context.
     * 
     * @param systemPrompt System instructions for the model
     * @param userQuery The user's actual query
     * @param retrievedContext Additional context from vector store / database
     * @return Model's response text
     */
    public String generateResponse(String systemPrompt, String userQuery, String retrievedContext) {
        if (!geminiConfig.getAssistant().isEnabled()) {
            log.warn("Gemini assistant is disabled");
            return null;
        }

        if (geminiConfig.getApi().getKey() == null || geminiConfig.getApi().getKey().isEmpty()) {
            log.error("Gemini API key not configured");
            throw new IllegalStateException("GEMINI_API_KEY is not set");
        }

        try {
            String url = buildApiUrl();
            Map<String, Object> requestBody = buildRequestBody(systemPrompt, userQuery, retrievedContext);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, getHeaders());
            
            log.info("Calling Gemini API at: {}", url);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            return parseGeminiResponse(response.getBody());

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Build the complete API URL with model name and API key.
     */
    private String buildApiUrl() {
        return String.format("%s/%s:generateContent?key=%s",
                geminiConfig.getApi().getEndpoint(),
                geminiConfig.getModel().getName(),
                geminiConfig.getApi().getKey());
    }

    /**
     * Build the request body according to Gemini API schema.
     * 
     * Structure:
     * {
     *   "contents": [{
     *     "parts": [
     *       { "text": "system prompt + context + query" }
     *     ]
     *   }],
     *   "generationConfig": {
     *     "temperature": 0.7,
     *     "maxOutputTokens": 1000
     *   }
     * }
     */
    private Map<String, Object> buildRequestBody(String systemPrompt, String userQuery, String retrievedContext) {
        Map<String, Object> requestBody = new HashMap<>();

        // Build the full prompt: system + context + query
        String fullPrompt = buildFullPrompt(systemPrompt, userQuery, retrievedContext);

        // Contents array with parts
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", fullPrompt);

        content.put("parts", Arrays.asList(part));
        requestBody.put("contents", Arrays.asList(content));

        // Generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", geminiConfig.getModel().getTemperature());
        generationConfig.put("maxOutputTokens", geminiConfig.getModel().getMaxTokens());
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    /**
     * Combine system prompt, retrieved context, and user query into a single prompt.
     */
    private String buildFullPrompt(String systemPrompt, String userQuery, String retrievedContext) {
        StringBuilder prompt = new StringBuilder();

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            prompt.append(systemPrompt).append("\n\n");
        }

        if (retrievedContext != null && !retrievedContext.isEmpty()) {
            prompt.append("=== RETRIEVED CONTEXT ===\n")
                    .append(retrievedContext)
                    .append("\n\n=== END CONTEXT ===\n\n");
        }

        prompt.append("User Query: ").append(userQuery);

        return prompt.toString();
    }

    /**
     * Extract text content from Gemini API response.
     * 
     * Response structure:
     * {
     *   "candidates": [{
     *     "content": {
     *       "parts": [{ "text": "..." }]
     *     }
     *   }]
     * }
     */
    private String parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // Navigate: candidates[0].content.parts[0].text
            JsonNode candidates = root.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                log.warn("No candidates in Gemini response");
                return null;
            }

            JsonNode firstCandidate = candidates.get(0);
            JsonNode content = firstCandidate.get("content");
            
            if (content == null) {
                log.warn("No content in first candidate");
                return null;
            }

            JsonNode parts = content.get("parts");
            if (parts == null || parts.isEmpty()) {
                log.warn("No parts in content");
                return null;
            }

            JsonNode textNode = parts.get(0).get("text");
            if (textNode == null) {
                log.warn("No text in first part");
                return null;
            }

            String responseText = textNode.asText();
            log.info("Gemini response received: {} chars", responseText.length());
            return responseText;

        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }

    /**
     * Get HTTP headers for Gemini API call.
     * Content-Type must be application/json.
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
