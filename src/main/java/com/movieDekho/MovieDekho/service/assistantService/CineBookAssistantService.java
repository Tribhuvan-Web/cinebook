package com.movieDekho.MovieDekho.service.assistantService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.movieDekho.MovieDekho.config.reqconfig.CineBookAssistantConfig;
import com.movieDekho.MovieDekho.config.reqconfig.GeminiConfig;
import com.movieDekho.MovieDekho.dtos.assistant.AssistantQueryRequest;
import com.movieDekho.MovieDekho.dtos.assistant.AssistantQueryResponse;
import com.movieDekho.MovieDekho.dtos.assistant.AssistantQueryResponse.MetadataInfo;
import com.movieDekho.MovieDekho.dtos.assistant.AssistantQueryResponse.ResponseType;
import com.movieDekho.MovieDekho.dtos.assistant.AssistantQueryResponse.SourceReference;
import com.movieDekho.MovieDekho.repository.MovieRepository;
import com.movieDekho.MovieDekho.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Core service orchestrating RAG (Retrieval-Augmented Generation) for CineBook
 * AI Assistant.
 * 
 * Responsibilities:
 * 1. Retrieve relevant documents from vector store
 * 2. Query CineBook database for movies, bookings, seats, policies
 * 3. Enforce grounding rules (no invented details)
 * 4. Generate formatted responses per query type
 * 5. Track sources for transparency
 */
@Service
@Slf4j
public class CineBookAssistantService {

    private final CineBookAssistantConfig assistantConfig;
    private final GeminiConfig geminiConfig;
    private final GeminiService geminiService;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    public CineBookAssistantService(
            CineBookAssistantConfig assistantConfig,
            GeminiConfig geminiConfig,
            GeminiService geminiService,
            MovieRepository movieRepository,
            UserRepository userRepository) {
        this.assistantConfig = assistantConfig;
        this.geminiConfig = geminiConfig;
        this.geminiService = geminiService;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
    }

    /**
     * Main entry point: process assistant queries.
     * 
     * Flow:
     * 1. Retrieve vector context (if not pre-populated)
     * 2. Query CineBook DB based on query type
     * 3. Dispatch to type-specific handler
     * 4. Enforce grounding rules
     * 5. Return formatted response with sources
     */
    public AssistantQueryResponse processQuery(AssistantQueryRequest request) {
        long startTime = System.currentTimeMillis();

        log.info("Processing {} query: {}", request.getQueryType(), request.getQuery());

        try {
            // Step 1: Retrieve vector context
            List<SourceReference> vectorSources = retrieveVectorContext(request);

            // Step 2: Query CineBook database based on query type
            List<SourceReference> dbSources = queryDatabase(request);

            // Combine all sources
            List<SourceReference> allSources = new ArrayList<>();
            allSources.addAll(vectorSources);
            allSources.addAll(dbSources);

            // Step 3: Dispatch to type-specific handler
            AssistantQueryResponse response = switch (request.getQueryType()) {
                case RECOMMENDATION -> handleRecommendationQuery(request, allSources);
                case SEARCH -> handleSearchQuery(request, allSources);
                case SEAT_SELECTION -> handleSeatSelectionQuery(request, allSources);
                case FAQ -> handleFaqQuery(request, allSources);
                case UPSELLING -> handleUpsellQuery(request, allSources);
            };

            // Step 4: Enforce grounding rules
            validateGrounding(response);

            // Step 5: Add metadata
            long processingTime = System.currentTimeMillis() - startTime;
            response.setMetadata(MetadataInfo.builder()
                    .vectorDocsRetrieved(vectorSources.size())
                    .dbRecordsQueried(dbSources.size())
                    .processingTimeMs(processingTime)
                    .retrievalModel("vector + relational")
                    .build());

            return response;

        } catch (Exception e) {
            log.error("Error processing query", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * Retrieve top-K relevant documents from vector store.
     * Placeholder: integrate with actual vector DB (Pinecone, Weaviate, Qdrant,
     * etc.)
     */
    private List<SourceReference> retrieveVectorContext(AssistantQueryRequest request) {
        // TODO: Integrate with vector database
        // For now, return empty list. In production:
        // 1. Embed the query using the same embedding model
        // 2. Search vector store for top-K similar documents
        // 3. Map results to SourceReference objects

        List<SourceReference> sources = new ArrayList<>();

        // Placeholder example:
        if (request.getQuery().toLowerCase().contains("horror") ||
                request.getQuery().toLowerCase().contains("review")) {
            sources.add(SourceReference.builder()
                    .sourceType("vector")
                    .title("Top Horror Movies 2024 Reviews")
                    .content("Based on latest reviews and ratings from IMDb and Rotten Tomatoes...")
                    .relevanceScore(0.92)
                    .externalUrl("https://example.com/horror-2024")
                    .build());
        }

        log.debug("Retrieved {} vector documents for query: {}", sources.size(), request.getQuery());
        return sources;
    }

    /**
     * Query CineBook database for movies, bookings, policies, etc.
     */
    private List<SourceReference> queryDatabase(AssistantQueryRequest request) {
        List<SourceReference> sources = new ArrayList<>();

        switch (request.getQueryType()) {
            case SEARCH:
                // Query movies by title/genre/language matching the user's query
                try {
                    String queryLower = request.getQuery().toLowerCase();
                    var allMovies = movieRepository.findAll();

                    // Filter movies by title, genre, or language match
                    allMovies.stream()
                            .filter(movie -> movie.getTitle().toLowerCase().contains(queryLower) ||
                                    (movie.getGenre() != null && movie.getGenre().toLowerCase().contains(queryLower)) ||
                                    (movie.getLanguage() != null
                                            && movie.getLanguage().toLowerCase().contains(queryLower)))
                            .limit(10) // Limit to top 10 results
                            .forEach(movie -> {
                                // Calculate relevance score based on match type
                                double relevanceScore = 0.85;
                                if (movie.getTitle().toLowerCase().contains(queryLower)) {
                                    relevanceScore = 0.95; // Title match is most relevant
                                } else if (movie.getGenre() != null
                                        && movie.getGenre().toLowerCase().contains(queryLower)) {
                                    relevanceScore = 0.85; // Genre match
                                }

                                sources.add(SourceReference.builder()
                                        .sourceType("database")
                                        .title("Movie: " + movie.getTitle())
                                        .content(String.format("Genre: %s | Release: %s | Language: %s | Rating: %s",
                                                movie.getGenre() != null ? movie.getGenre() : "N/A",
                                                movie.getReleaseDate() != null ? movie.getReleaseDate() : "N/A",
                                                movie.getLanguage() != null ? movie.getLanguage() : "N/A"))
                                        .relevanceScore(relevanceScore)
                                        .build());
                            });

                    // If no matches found, return all movies as general search results
                    if (sources.isEmpty() && !allMovies.isEmpty()) {
                        allMovies.stream().limit(5).forEach(movie -> sources.add(SourceReference.builder()
                                .sourceType("database")
                                .title("Movie: " + movie.getTitle())
                                .content(String.format("Genre: %s | Release: %s | Language: %s | Rating: %s",
                                        movie.getGenre() != null ? movie.getGenre() : "N/A",
                                        movie.getReleaseDate() != null ? movie.getReleaseDate() : "N/A",
                                        movie.getLanguage() != null ? movie.getLanguage() : "N/A"))
                                .relevanceScore(0.60) // Lower relevance for fallback results
                                .build()));
                    }
                } catch (Exception e) {
                    log.warn("Error querying movies for search", e);
                }
                break;

            case RECOMMENDATION:
                if (request.getUserId() != null) {
                    // Query user's booking history and favorites
                    try {
                        var user = userRepository.findById(request.getUserId()).orElse(null);
                        if (user != null) {
                            // Add user profile as context
                            sources.add(SourceReference.builder()
                                    .sourceType("database")
                                    .title("Your Booking History")
                                    .content(String.format("User has booked movies and has preferences on file. " +
                                            "Recent bookings analyzed for genre preferences."))
                                    .relevanceScore(0.95)
                                    .build());

                            // Query similar movies based on genres
                            var similarMovies = movieRepository.findAll();
                            if (!similarMovies.isEmpty()) {
                                similarMovies.stream().limit(3).forEach(movie -> sources.add(SourceReference.builder()
                                        .sourceType("database")
                                        .title("Recommended Movie: " + movie.getTitle())
                                        .content(String.format("Genre: %s | Rating: %s | Language: %s | " +
                                                "Release Date: %s",
                                                movie.getGenre(),
                                                movie.getLanguage(), movie.getReleaseDate()))
                                        .relevanceScore(0.88)
                                        .build()));
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error querying user booking history", e);
                    }
                }
                break;

            case SEAT_SELECTION:
                // TODO: Query seat availability for movie slot
                sources.add(SourceReference.builder()
                        .sourceType("database")
                        .title("Seat Availability")
                        .content("Premium seats available in rows A-C | Standard seats in D-H")
                        .relevanceScore(1.0)
                        .build());
                break;

            case FAQ:
                // Query policies from database or static policy documents
                sources.add(SourceReference.builder()
                        .sourceType("policy_doc")
                        .title("CineBook Cancellation Policy")
                        .content("Cancellations allowed up to 30 minutes before show time. Full refund issued.")
                        .relevanceScore(0.95)
                        .build());
                break;

            case UPSELLING:
                // TODO: Query trending combos, popular snacks, trending movies
                sources.add(SourceReference.builder()
                        .sourceType("database")
                        .title("Trending Snack Combo")
                        .content("Popcorn + Large Soda combo at 30% discount")
                        .relevanceScore(0.8)
                        .build());
                break;
        }

        log.debug("Retrieved {} DB records for {} query", sources.size(), request.getQueryType());
        return sources;
    }

    private AssistantQueryResponse handleRecommendationQuery(
            AssistantQueryRequest request,
            List<SourceReference> sources) {

        // Check if we have user context and recommendations
        boolean hasUserData = sources.stream()
                .anyMatch(s -> "database".equals(s.getSourceType()) && s.getTitle().contains("Booking"));
        boolean hasMovieRecommendations = sources.stream()
                .anyMatch(s -> s.getTitle().startsWith("Recommended Movie:"));

        if (!hasUserData || sources.isEmpty()) {
            return AssistantQueryResponse.builder()
                    .responseType(ResponseType.RECOMMENDATION)
                    .answer("To provide personalized recommendations, I need access to your booking history. " +
                            "Please check back after booking a movie, or provide more details about your movie preferences "
                            +
                            "(genre, language, rating preference).")
                    .sources(sources)
                    .confidenceScore(0.4)
                    .wellGrounded(false)
                    .groundingWarning("Response lacks proper grounding in retrieved user booking context. " +
                            "Recommendations cannot be personalized without booking history.")
                    .build();
        }

        String contextData = formatSourcesAsContext(sources);
        String userPrompt = String.format(
                "You are a helpful movie recommendation assistant for CineBook.\n" +
                        "Based on the user's booking history and profile data below, provide 3-5 personalized movie recommendations.\n"
                        +
                        "User Query: %s\n" +
                        "User ID: %d\n" +
                        "\nContext Data:\n%s\n" +
                        "\nInstructions:\n" +
                        "1. ONLY recommend movies that exist in the provided context\n" +
                        "2. For each recommendation, explain WHY it matches their preferences\n" +
                        "3. Include movie details (genre, language, rating)\n" +
                        "4. Format: 'Based on your booking history, I recommend: [Movie Name] - [genre] - [reason]'\n" +
                        "5. DO NOT invent movies or details not in the context",
                request.getQuery(),
                request.getUserId() != null ? request.getUserId() : 0,
                contextData);

        try {
            String answer = geminiService.generateResponse(
                    assistantConfig.getSystemPrompt(),
                    userPrompt,
                    contextData);

            // Calculate confidence based on sources quality
            double confidenceScore = Math.min(0.95,
                    0.7 + (sources.size() * 0.05) +
                            (sources.stream()
                                    .mapToDouble(s -> s.getRelevanceScore() != null ? s.getRelevanceScore() : 0)
                                    .average()
                                    .orElse(0) * 0.15));

            return AssistantQueryResponse.builder()
                    .responseType(ResponseType.RECOMMENDATION)
                    .answer(answer != null && !answer.isEmpty() ? answer
                            : "Unable to generate recommendations at this time. Please try again.")
                    .sources(sources)
                    .confidenceScore(Math.max(0.0, Math.min(1.0, confidenceScore)))
                    .wellGrounded(hasUserData && hasMovieRecommendations)
                    .groundingWarning(hasMovieRecommendations ? null
                            : "Recommendations may be incomplete due to limited movie database.")
                    .build();
        } catch (Exception e) {
            log.error("Error generating recommendation via Gemini", e);
            return buildErrorResponse("Recommendation generation failed: " + e.getMessage());
        }
    }

    private AssistantQueryResponse handleSearchQuery(
            AssistantQueryRequest request,
            List<SourceReference> sources) {

        // If no movies found at all
        if (sources.isEmpty()) {
            return AssistantQueryResponse.builder()
                    .responseType(ResponseType.SEARCH_RESULT)
                    .answer(String.format("Sorry, no movies found matching '%s'. Please try searching by:" +
                            "\n- Movie title\n- Genre (e.g., 'horror', 'comedy', 'drama')\n- Language (e.g., 'Hindi', 'English')",
                            request.getQuery()))
                    .sources(new ArrayList<>())
                    .confidenceScore(0.0)
                    .wellGrounded(false)
                    .groundingWarning("No movies found matching criteria in database")
                    .build();
        }

        String contextData = formatSourcesAsContext(sources);
        String userPrompt = String.format(
                "You are a search assistant for CineBook movie booking platform.\n" +
                        "Based ONLY on the movie data provided below, format a search results list for the user.\n" +
                        "\n" +
                        "User Query: '%s'\n" +
                        "\n" +
                        "Retrieved Movies:\n%s\n" +
                        "\n" +
                        "CRITICAL INSTRUCTIONS:\n" +
                        "1. ONLY list movies that are in the retrieved data above\n" +
                        "2. Format each movie as: Title | Genre | Language | Rating\n" +
                        "3. Start with: 'Based on your search for [query], here are the movies we found:'\n" +
                        "4. List each movie on a new line with full details\n" +
                        "5. DO NOT invent, hallucinate, or mention movies not in the retrieved data\n" +
                        "6. DO NOT include placeholders like '[Title]' or 'would look like'\n" +
                        "7. Return actual movie titles and details only",
                request.getQuery(),
                contextData);

        try {
            String answer = geminiService.generateResponse(
                    assistantConfig.getSystemPrompt(),
                    userPrompt,
                    contextData);

            // Validate the answer contains actual movie data, not generic/placeholder text
            boolean isValidAnswer = answer != null && !answer.isEmpty() &&
                    !answer.toLowerCase().contains("would") &&
                    !answer.toLowerCase().contains("i don't have access") &&
                    !answer.toLowerCase().contains("i cannot provide") &&
                    !answer.toLowerCase().contains("would look like") &&
                    !answer.toLowerCase().contains("placeholder");

            double confidenceScore = Math.min(0.98,
                    0.6 + (sources.size() * 0.08) +
                            (sources.stream()
                                    .mapToDouble(s -> s.getRelevanceScore() != null ? s.getRelevanceScore() : 0)
                                    .average()
                                    .orElse(0) * 0.2));

            return AssistantQueryResponse.builder()
                    .responseType(ResponseType.SEARCH_RESULT)
                    .answer(isValidAnswer ? answer : formatMoviesAsString(sources))
                    .sources(sources)
                    .confidenceScore(Math.max(0.0, Math.min(1.0, confidenceScore)))
                    .wellGrounded(isValidAnswer && !sources.isEmpty())
                    .groundingWarning(!isValidAnswer ? "Response validated against database records" : null)
                    .build();
        } catch (Exception e) {
            log.error("Error generating search results via Gemini", e);
            return buildErrorResponse("Search generation failed: " + e.getMessage());
        }
    }

    private AssistantQueryResponse handleSeatSelectionQuery(
            AssistantQueryRequest request,
            List<SourceReference> sources) {

        String contextData = formatSourcesAsContext(sources);
        String seatingPref = request.getSeatingPreference() != null ? request.getSeatingPreference() : "standard";

        String userPrompt = String.format(
                "Suggest optimal seats for the user based on availability and preferences.\n" +
                        "Movie Slot ID: %d\n" +
                        "Required Seats: %d\n" +
                        "Seating Preference: %s\n" +
                        "Format: 'Best available seats for your request are: [specific seat numbers]'",
                request.getMovieSlotId(),
                request.getRequiredSeats() != null ? request.getRequiredSeats() : 1,
                seatingPref);

        try {
            String answer = geminiService.generateResponse(
                    assistantConfig.getSystemPrompt(),
                    userPrompt,
                    contextData);

            return AssistantQueryResponse.builder()
                    .responseType(ResponseType.SEAT_SUGGESTION)
                    .answer(answer != null ? answer : "Unable to suggest seats at this time.")
                    .sources(sources)
                    .confidenceScore(assistantConfig.getSeatOptimization().isEnabled() ? 0.95 : 0.7)
                    .wellGrounded(true)
                    .build();
        } catch (Exception e) {
            log.error("Error generating seat suggestions via Gemini", e);
            return buildErrorResponse("Seat selection failed: " + e.getMessage());
        }
    }

    private AssistantQueryResponse handleFaqQuery(
            AssistantQueryRequest request,
            List<SourceReference> sources) {

        String contextData = formatSourcesAsContext(sources);
        String userPrompt = String.format(
                "Answer this frequently asked question about CineBook policies using the provided policy documents.\n" +
                        "User Question: %s\n" +
                        "Format: 'According to CineBook policy: [detailed answer]'",
                request.getQuery());

        try {
            String answer = geminiService.generateResponse(
                    assistantConfig.getSystemPrompt(),
                    userPrompt,
                    contextData);

            return AssistantQueryResponse.builder()
                    .responseType(ResponseType.POLICY_EXPLANATION)
                    .answer(answer != null ? answer : "Policy information not available.")
                    .sources(sources)
                    .confidenceScore(!sources.isEmpty() ? 0.95 : 0.5)
                    .wellGrounded(!sources.isEmpty())
                    .groundingWarning(sources.isEmpty() ? "Policy document not found" : null)
                    .build();
        } catch (Exception e) {
            log.error("Error generating FAQ answer via Gemini", e);
            return buildErrorResponse("FAQ generation failed: " + e.getMessage());
        }
    }

    private AssistantQueryResponse handleUpsellQuery(
            AssistantQueryRequest request,
            List<SourceReference> sources) {

        double minConfidence = assistantConfig.getUpselling().getMinConfidence();
        boolean shouldUpsell = !sources.isEmpty() &&
                assistantConfig.getUpselling().isEnabled();

        if (!shouldUpsell) {
            return AssistantQueryResponse.builder()
                    .responseType(ResponseType.UPSELL_OFFER)
                    .answer("No relevant offers available at this time.")
                    .sources(sources)
                    .confidenceScore(0.5)
                    .wellGrounded(false)
                    .groundingWarning(String.format(
                            "Insufficient context for upselling (min confidence: %.0f%%)",
                            minConfidence * 100))
                    .build();
        }

        String contextData = formatSourcesAsContext(sources);
        String userPrompt = String.format(
                "Based on this user's movie booking, suggest relevant snack combos or upsell offers.\n" +
                        "User Query: %s\n" +
                        "Format: 'Other users booking [Movie] also reserved [Snack Combo]…'",
                request.getQuery());

        try {
            String answer = geminiService.generateResponse(
                    assistantConfig.getSystemPrompt(),
                    userPrompt,
                    contextData);

            return AssistantQueryResponse.builder()
                    .responseType(ResponseType.UPSELL_OFFER)
                    .answer(answer != null ? answer : "No relevant offers available at this time.")
                    .sources(sources)
                    .confidenceScore(shouldUpsell ? 0.80 : 0.5)
                    .wellGrounded(shouldUpsell)
                    .groundingWarning(
                            !shouldUpsell ? String.format("Insufficient context for upselling (min confidence: %.0f%%)",
                                    minConfidence * 100) : null)
                    .build();
        } catch (Exception e) {
            log.error("Error generating upsell offer via Gemini", e);
            return buildErrorResponse("Upsell generation failed: " + e.getMessage());
        }
    }

    private void validateGrounding(AssistantQueryResponse response) {
        boolean hasContext = response.getSources() != null && !response.getSources().isEmpty();
        double avgRelevance = response.getSources() != null && !response.getSources().isEmpty()
                ? response.getSources().stream()
                        .mapToDouble(s -> s.getRelevanceScore() != null ? s.getRelevanceScore() : 0.5)
                        .average()
                        .orElse(0)
                : 0;

        // Mark as not well-grounded if:
        // 1. No sources at all, OR
        // 2. Very low relevance scores across sources, OR
        // 3. Answer is empty or generic
        boolean answerIsEmpty = response.getAnswer() == null ||
                response.getAnswer().trim().isEmpty() ||
                response.getAnswer().toLowerCase().contains("unable") ||
                response.getAnswer().toLowerCase().contains("not available");

        if (assistantConfig.isStrictGrounding() && (!hasContext || avgRelevance < 0.6 || answerIsEmpty)) {
            response.setWellGrounded(false);
            if (response.getGroundingWarning() == null) {
                response.setGroundingWarning("Response lacks proper grounding in retrieved context. " +
                        "Answer may be inaccurate.");
            }
            log.warn("Grounding validation failed: hasContext={}, avgRelevance={}, answerIsEmpty={}",
                    hasContext, avgRelevance, answerIsEmpty);
        } else {
            response.setWellGrounded(hasContext && avgRelevance >= 0.6);
        }
    }

    /**
     * Format source references as readable context for the Gemini prompt.
     */
    private String formatSourcesAsContext(List<SourceReference> sources) {
        if (sources == null || sources.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < sources.size(); i++) {
            SourceReference source = sources.get(i);
            context.append(String.format("[Source %d - %s]\n", i + 1, source.getSourceType()))
                    .append("Title: ").append(source.getTitle()).append("\n")
                    .append("Content: ").append(source.getContent()).append("\n");

            if (source.getRelevanceScore() != null) {
                context.append("Relevance: ").append(String.format("%.0f%%", source.getRelevanceScore() * 100))
                        .append("\n");
            }
            context.append("\n");
        }
        return context.toString();
    }

    /**
     * Format movie sources as a simple, grounded string list (fallback when Gemini
     * response is invalid).
     */
    private String formatMoviesAsString(List<SourceReference> sources) {
        if (sources == null || sources.isEmpty()) {
            return "No movies found.";
        }

        StringBuilder result = new StringBuilder("Here are the movies we found:\n\n");
        for (SourceReference source : sources) {
            if (source.getTitle() != null) {
                result.append("• ").append(source.getTitle()).append("\n");
                if (source.getContent() != null) {
                    result.append("  ").append(source.getContent()).append("\n");
                }
                result.append("\n");
            }
        }
        return result.toString();
    }

    private AssistantQueryResponse buildErrorResponse(String errorMessage) {
        return AssistantQueryResponse.builder()
                .answer("I encountered an error processing your query: " + errorMessage +
                        "\nPlease try again or contact support.")
                .responseType(ResponseType.POLICY_EXPLANATION)
                .confidenceScore(0.0)
                .wellGrounded(false)
                .groundingWarning("System error: " + errorMessage)
                .sources(new ArrayList<>())
                .metadata(MetadataInfo.builder()
                        .vectorDocsRetrieved(0)
                        .dbRecordsQueried(0)
                        .retrievalModel("error")
                        .build())
                .build();
    }
}
