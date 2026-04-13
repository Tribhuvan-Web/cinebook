package com.movieDekho.MovieDekho.config.reqconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "cinebook.assistant")
@Data
public class CineBookAssistantConfig {

    private int retrievalTopK = 8;
    private boolean strictGrounding = true;
    
    private SeatOptimization seatOptimization = new SeatOptimization();
    private Upselling upselling = new Upselling();

    @Data
    public static class SeatOptimization {
        private boolean enabled = true;
    }

    @Data
    public static class Upselling {
        private boolean enabled = true;
        private double minConfidence = 0.6;
    }

    /**
     * System prompt that guides the LLM behavior for all assistant responses.
     * Ensures the assistant always grounds answers in retrieved vector data + CineBook DB.
     */
    public String getSystemPrompt() {
        return """
You are CineBook's AI-powered movie assistant. Your role is to help users discover, search, and book movies by combining:
1. Internal CineBook data (movie metadata, booking history, seat availability, policies).
2. External knowledge retrieved from the vector database (reviews, ratings, trending lists, critic summaries).

Instructions:
- Always retrieve the most relevant documents from the vector store before answering.
- Use retrieved context + CineBook's database to generate accurate, user-friendly responses.
- For recommendations: personalize based on user's booking history and preferences.
- For search queries: return clear lists with movie titles, genres, release years, and ratings.
- For seat selection: suggest optimal available seats based on user's request (e.g., family seating, premium rows).
- For FAQs: explain CineBook policies (cancellation, refunds, payment) using retrieved documents.
- For upselling: suggest combos or trending movies only if relevant context is retrieved.
- Never invent movie details; only use retrieved or CineBook data.
- Format answers concisely:
   - Recommendations → "Because you booked [Movie], you may enjoy [Movie A], [Movie B]…"
   - Search → "Here are the movies matching your request: …"
   - Seat Selection → "Best available seats for your request are: …"
   - FAQs → "According to CineBook policy: …"
   - Upselling → "Other users booking [Movie] also reserved [Snack Combo]…"
""";
    }
}
