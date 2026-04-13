package com.movieDekho.MovieDekho.dtos.assistant;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload from CineBook AI Assistant.
 * Includes answer, sources used, and grounding metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssistantQueryResponse {
    
    /**
     * The assistant's formatted response text (ready to display to user)
     */
    private String answer;
    
    /**
     * Type of answer: RECOMMENDATION, SEARCH_RESULT, SEAT_SUGGESTION, POLICY_EXPLANATION, UPSELL_OFFER
     */
    private ResponseType responseType;
    
    /**
     * Sources retrieved from vector store and CineBook DB used to generate answer
     */
    private List<SourceReference> sources;
    
    /**
     * Confidence score (0.0-1.0) that the answer is grounded and accurate
     */
    private double confidenceScore;
    
    /**
     * Whether answer strictly respects grounding rules (no invented details)
     */
    private boolean wellGrounded;
    
    /**
     * If grounding failed and strict mode is enabled, error message
     */
    private String groundingWarning;
    
    /**
     * Additional metadata: retrieved vector docs count, DB queries executed, etc.
     */
    private MetadataInfo metadata;
    
    public enum ResponseType {
        RECOMMENDATION, SEARCH_RESULT, SEAT_SUGGESTION, POLICY_EXPLANATION, UPSELL_OFFER
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SourceReference {
        private String sourceType; // "vector", "database", "policy_doc"
        private String title;
        private String content;
        private Double relevanceScore;
        private String externalUrl; // Link to full review, policy, etc.
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetadataInfo {
        private int vectorDocsRetrieved;
        private int dbRecordsQueried;
        private long processingTimeMs;
        private String retrievalModel;
    }
}
