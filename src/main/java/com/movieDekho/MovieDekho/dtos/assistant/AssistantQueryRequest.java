package com.movieDekho.MovieDekho.dtos.assistant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for CineBook AI Assistant queries.
 * Supports multiple query types: recommendation, search, seat_selection, faq, upselling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssistantQueryRequest {
    
    /**
     * Type of query: RECOMMENDATION, SEARCH, SEAT_SELECTION, FAQ, UPSELLING
     */
    private QueryType queryType;
    
    /**
     * User's query text (e.g., "Find horror movies released in 2024")
     */
    private String query;
    
    /**
     * User ID for personalized recommendations based on booking history
     */
    private Long userId;
    
    /**
     * Movie slot ID for seat selection queries
     */
    private Long movieSlotId;
    
    /**
     * Number of seats to suggest (for seat selection queries)
     */
    private Integer requiredSeats;
    
    /**
     * Seating preference (e.g., "family", "premium", "couple", "aisle")
     */
    private String seatingPreference;
    
    /**
     * Conversation context: previous messages for multi-turn chat
     */
    private String conversationContext;
    
    /**
     * Optional context from vector store retrieval (pre-populated or empty for auto-retrieval)
     */
    private String vectorContextData;
    
    public enum QueryType {
        RECOMMENDATION, SEARCH, SEAT_SELECTION, FAQ, UPSELLING
    }
}
