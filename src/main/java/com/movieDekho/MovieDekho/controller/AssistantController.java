package com.movieDekho.MovieDekho.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movieDekho.MovieDekho.dtos.assistant.AssistantQueryRequest;
import com.movieDekho.MovieDekho.dtos.assistant.AssistantQueryResponse;
import com.movieDekho.MovieDekho.service.assistantService.CineBookAssistantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API for CineBook AI Assistant.
 * 
 * Endpoints support multiple query types:
 * - RECOMMENDATION: Get personalized movie recommendations
 * - SEARCH: Find movies by title/genre/criteria
 * - SEAT_SELECTION: Get optimal seat suggestions
 * - FAQ: Get policy and FAQ answers
 * - UPSELLING: Get contextual combo/snack offers
 * 
 * All responses are grounded in retrieved vector data + CineBook database.
 */
@RestController
@RequestMapping("/api/assistant")
@Tag(name = "CineBook AI Assistant", description = "RAG-powered movie discovery and booking assistant")
@Slf4j
public class AssistantController {

    private final CineBookAssistantService assistantService;
    private final com.movieDekho.MovieDekho.config.reqconfig.CineBookAssistantConfig assistantConfig;

    public AssistantController(
            CineBookAssistantService assistantService,
            com.movieDekho.MovieDekho.config.reqconfig.CineBookAssistantConfig assistantConfig) {
        this.assistantService = assistantService;
        this.assistantConfig = assistantConfig;
    }

    /**
     * Process assistant query and return grounded response.
     * 
     * Supports all query types: RECOMMENDATION, SEARCH, SEAT_SELECTION, FAQ, UPSELLING
     * 
     * @param request Contains query text, type, user context, and optional pre-retrieved vector data
     * @return Response with answer, sources, confidence score, and grounding metadata
     */
    @PostMapping("/query")
    @Operation(
            summary = "Query CineBook AI Assistant",
            description = "Submit a query for movie discovery, booking help, or policy information. " +
                    "Assistant uses RAG to combine vector-retrieved context with CineBook database."
    )
    public ResponseEntity<AssistantQueryResponse> queryAssistant(
            @RequestBody AssistantQueryRequest request) {
        
        log.info("Received {} query from user {}: {}",
                request.getQueryType(), request.getUserId(), request.getQuery());

        // Validate input
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            log.warn("Query is empty");
            return ResponseEntity.badRequest().body(
                    AssistantQueryResponse.builder()
                            .answer("Please provide a valid query.")
                            .wellGrounded(false)
                            .confidenceScore(0.0)
                            .groundingWarning("Invalid input: empty query")
                            .build()
            );
        }

        try {
            AssistantQueryResponse response = assistantService.processQuery(request);
            
            // Determine appropriate HTTP status based on grounding quality
            HttpStatus status;
            if (response.isWellGrounded() || !assistantConfig.isStrictGrounding()) {
                status = HttpStatus.OK;
            } else if (response.getConfidenceScore() >= 0.5) {
                status = HttpStatus.PARTIAL_CONTENT; // 206: Incomplete but acceptable
            } else {
                status = HttpStatus.INSUFFICIENT_STORAGE; // 507: Insufficient data
            }
            
            log.info("Assistant response: wellGrounded={}, confidence={}, sources={}",
                    response.isWellGrounded(),
                    response.getConfidenceScore(),
                    response.getSources() != null ? response.getSources().size() : 0);
            
            return new ResponseEntity<>(response, status);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid query request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    AssistantQueryResponse.builder()
                            .answer("Invalid query format. Please check your request.")
                            .wellGrounded(false)
                            .groundingWarning(e.getMessage())
                            .confidenceScore(0.0)
                            .build()
            );
        } catch (Exception e) {
            log.error("Unexpected error processing assistant query", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AssistantQueryResponse.builder()
                            .answer("An error occurred processing your query. Our team has been notified.")
                            .wellGrounded(false)
                            .groundingWarning("System error: " + e.getMessage())
                            .confidenceScore(0.0)
                            .build()
            );
        }
    }

    /**
     * Health check endpoint for assistant service.
     */
    @PostMapping("/health")
    @Operation(summary = "Check assistant service health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("CineBook Assistant service is running.");
    }
}
