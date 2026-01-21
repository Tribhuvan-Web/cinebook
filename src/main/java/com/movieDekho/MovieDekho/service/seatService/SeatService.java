package com.movieDekho.MovieDekho.service.seatService;

import com.movieDekho.MovieDekho.dtos.seat.SeatRequest;
import com.movieDekho.MovieDekho.dtos.seat.SeatResponse;
import com.movieDekho.MovieDekho.dtos.seat.SeatUpdateRequest;
import com.movieDekho.MovieDekho.exception.ResourceNotFoundException;
import com.movieDekho.MovieDekho.models.MovieSlot;
import com.movieDekho.MovieDekho.models.Seat;
import com.movieDekho.MovieDekho.repository.MovieSlotRepository;
import com.movieDekho.MovieDekho.repository.SeatRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class SeatService {

    private final SeatRepository seatRepository;
    private final MovieSlotRepository movieSlotRepository;

    /**
     * OPTIMIZED: Create seats with better batching and performance
     * Time Complexity: O(n) where n = number of seat requests
     * Database Calls: 3 (slot validation + duplicate check + batch insert)
     */
    @Transactional
    public List<SeatResponse> createSeats(Long slotId, List<SeatRequest> seatRequests) {
        // Input validation
        if (seatRequests == null || seatRequests.isEmpty()) {
            throw new IllegalArgumentException("Seat requests cannot be null or empty");
        }
        
        // Limit batch size to prevent memory issues
        if (seatRequests.size() > 500) {
            throw new IllegalArgumentException("Maximum 500 seats can be created in a single batch. Current: " + seatRequests.size());
        }

        // O(1) - Single database call for slot validation
        MovieSlot slot = getMovieSlotById(slotId);

        // O(n) - Extract seat numbers efficiently with validation
        Set<String> requestSeatNumbers = new HashSet<>();
        for (SeatRequest request : seatRequests) {
            validateSeatRequest(request);
            if (!requestSeatNumbers.add(request.getSeatNumber())) {
                throw new IllegalArgumentException("Duplicate seat number in request: " + request.getSeatNumber());
            }
        }

        // O(1) - Single database query to check existing seats
        Set<String> existingSeatNumbers = seatRepository.findExistingSeatNumbers(slotId, requestSeatNumbers);
        if (!existingSeatNumbers.isEmpty()) {
            throw new IllegalArgumentException("Seat numbers already exist: " + existingSeatNumbers);
        }

        // O(n) - Create seat entities in batch
        List<Seat> seats = new ArrayList<>(seatRequests.size());
        for (SeatRequest request : seatRequests) {
            seats.add(createSeatEntity(slot, request));
        }

        // O(1) - Single batch database operation
        List<Seat> savedSeats = seatRepository.saveAllInBatch(seats);

        // O(1) - Update slot totals once
        updateSlotTotals(slot, seats.size(), seats.size());

        log.info("Created {} seats for slot {} in optimized manner", seats.size(), slotId);

        // O(n) - Convert to response efficiently
        return savedSeats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    /**
     * OPTIMIZED: Get all seats for slot with pagination support
     * Time Complexity: O(n) but with controlled memory usage
     * Database Calls: 2 (slot validation + seat query)
     */
    public List<SeatResponse> getAllSeatsForSlot(Long slotId) {
        // O(1) - Single database call
        MovieSlot slot = getMovieSlotById(slotId);
        
        // O(n) - Optimized query with specific fields only
        List<Seat> seats = seatRepository.findBySlotOrderBySeatNumber(slot);
        
        // O(n) - Stream conversion with pre-sized collection
        List<SeatResponse> responses = new ArrayList<>(seats.size());
        for (Seat seat : seats) {
            responses.add(convertToSeatResponse(seat));
        }
        
        return responses;
    }

    /**
     * OPTIMIZED: Get all seats with pagination for large datasets
     * Time Complexity: O(1) for database query with LIMIT/OFFSET
     */
    public List<SeatResponse> getAllSeatsForSlotPaginated(Long slotId, int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        
        MovieSlot slot = getMovieSlotById(slotId);
        List<Seat> seats = seatRepository.findBySlotWithPagination(slot, page * size, size);
        
        return seats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    /**
     * OPTIMIZED: Update seat with minimal database calls
     * Time Complexity: O(1)
     * Database Calls: 2-3 (get seat + optional validation + update)
     */
    @Transactional
    public SeatResponse updateSeat(Long seatId, SeatUpdateRequest request) {
        // O(1) - Single database call
        Seat seat = getSeatById(seatId);
        boolean hasChanges = false;

        // Only validate and update if seat number actually changes
        if (request.getSeatNumber() != null && !request.getSeatNumber().equals(seat.getSeatNumber())) {
            validateUniqueSeatNumber(seat, request.getSeatNumber());
            seat.setSeatNumber(request.getSeatNumber());
            hasChanges = true;
        }
        
        if (request.getPrice() > 0 && request.getPrice() != seat.getPrice()) {
            seat.setPrice(request.getPrice());
            hasChanges = true;
        }
        
        if (request.getBooked() != null && request.getBooked() != seat.isBooked()) {
            updateSeatBookingStatus(seat, request.getBooked());
            hasChanges = true;
        }

        // Only save if there are actual changes
        if (hasChanges) {
            seat = seatRepository.save(seat);
        }
        
        return convertToSeatResponse(seat);
    }

    /**
     * OPTIMIZED: Delete seat with batch slot update
     * Time Complexity: O(1)
     * Database Calls: 3 (get seat + delete + slot update)
     */
    @Transactional
    public void deleteSeat(Long seatId) {
        // O(1) - Single database call
        Seat seat = getSeatById(seatId);
        MovieSlot slot = seat.getSlot();
        boolean wasAvailable = !seat.isBooked();

        // O(1) - Delete operation
        seatRepository.delete(seat);

        // O(1) - Update slot totals in single operation
        int totalSeatsChange = -1;
        int availableSeatsChange = wasAvailable ? -1 : 0;
        updateSlotTotals(slot, totalSeatsChange, availableSeatsChange);
        
        log.info("Deleted seat {} from slot {}", seatId, slot.getSlotId());
    }

    /**
     * OPTIMIZED: Bulk delete seats
     * Time Complexity: O(1) for database operations
     */
    @Transactional
    public void deleteSeats(List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }
        
        // Get all seats in single query
        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new ResourceNotFoundException("Some seats not found");
        }
        
        // Group by slot for efficient slot updates
        Map<MovieSlot, List<Seat>> seatsBySlot = seats.stream()
                .collect(Collectors.groupingBy(Seat::getSlot));
        
        // Batch delete
        seatRepository.deleteAll(seats);
        
        // Update each slot once
        for (Map.Entry<MovieSlot, List<Seat>> entry : seatsBySlot.entrySet()) {
            MovieSlot slot = entry.getKey();
            List<Seat> slotSeats = entry.getValue();
            
            int totalChange = -slotSeats.size();
            int availableChange = (int) -slotSeats.stream().filter(s -> !s.isBooked()).count();
            
            updateSlotTotals(slot, totalChange, availableChange);
        }
    }

    /**
     * OPTIMIZED: Update seat booking status with change detection
     * Time Complexity: O(1)
     * Database Calls: 2 (get seat + update)
     */
    @Transactional
    public SeatResponse updateSeatBookingStatus(Long seatId, boolean isBooked) {
        // O(1) - Single database call
        Seat seat = getSeatById(seatId);
        boolean previousBookingStatus = seat.isBooked();

        // Only update if status actually changes
        if (previousBookingStatus != isBooked) {
            seat.setBooked(isBooked);
            
            // O(1) - Update slot availability count
            updateAvailableSeatsCount(seat.getSlot(), previousBookingStatus, isBooked);
            
            // O(1) - Save seat
            seat = seatRepository.save(seat);
            
            log.info("Updated seat {} booking status from {} to {}", seatId, previousBookingStatus, isBooked);
        }

        return convertToSeatResponse(seat);
    }

    /**
     * OPTIMIZED: Bulk update seat booking status
     * Time Complexity: O(n) for validation + O(1) for database operations
     */
    @Transactional
    public List<SeatResponse> updateMultipleSeatBookingStatus(List<Long> seatIds, boolean isBooked) {
        if (seatIds == null || seatIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Single query to get all seats
        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new ResourceNotFoundException("Some seats not found");
        }
        
        // Track changes by slot
        Map<MovieSlot, Integer> slotAvailabilityChanges = new HashMap<>();
        List<Seat> seatsToUpdate = new ArrayList<>();
        
        for (Seat seat : seats) {
            if (seat.isBooked() != isBooked) {
                seat.setBooked(isBooked);
                seatsToUpdate.add(seat);
                
                // Track availability changes per slot
                MovieSlot slot = seat.getSlot();
                int change = isBooked ? -1 : 1; // -1 if booking (reducing availability), +1 if freeing
                slotAvailabilityChanges.merge(slot, change, Integer::sum);
            }
        }
        
        if (!seatsToUpdate.isEmpty()) {
            // Batch update seats
            List<Seat> updatedSeats = seatRepository.saveAll(seatsToUpdate);
            
            // Update slot availability counts
            for (Map.Entry<MovieSlot, Integer> entry : slotAvailabilityChanges.entrySet()) {
                MovieSlot slot = entry.getKey();
                int availabilityChange = entry.getValue();
                if (availabilityChange != 0) {
                    slot.setAvailableSeats(slot.getAvailableSeats() + availabilityChange);
                    movieSlotRepository.save(slot);
                }
            }
            
            return updatedSeats.stream()
                    .map(this::convertToSeatResponse)
                    .collect(Collectors.toList());
        }
        
        // Return original seats if no changes
        return seats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    /**
     * HIGHLY OPTIMIZED: Bulk create seats with pattern and comprehensive validation
     * Time Complexity: O(r × s) for generation + O(1) for database operations
     * Database Calls: 3 (slot validation + duplicate check + batch insert)
     * Memory Optimization: Efficient seat generation with pre-sized collections
     */
    @Transactional
    public List<SeatResponse> bulkCreateSeats(Long slotId, String rowStart, String rowEnd,
                                              int seatsPerRow, double price) {
        
        // Input validation
        validateBulkCreateParameters(rowStart, rowEnd, seatsPerRow, price);
        
        // Calculate total seats and validate limits
        int totalRows = calculateRowCount(rowStart, rowEnd);
        int totalSeats = totalRows * seatsPerRow;
        
        if (totalSeats > 1000) {
            throw new IllegalArgumentException(
                String.format("Operation would create %d seats. Maximum allowed: 1000", totalSeats));
        }
        
        // O(1) - Single database call for slot validation
        MovieSlot slot = getMovieSlotById(slotId);
        
        // O(r × s) - Generate seats efficiently with pre-sized collection
        List<Seat> seats = generateSeatsOptimized(slot, rowStart, rowEnd, seatsPerRow, price, totalSeats);
        
        // O(n) - Extract seat numbers efficiently
        Set<String> seatNumbers = new HashSet<>(totalSeats);
        for (Seat seat : seats) {
            seatNumbers.add(seat.getSeatNumber());
        }
        
        // O(1) - Single database query to check existing seats
        Set<String> existingSeatNumbers = seatRepository.findExistingSeatNumbers(slotId, seatNumbers);
        if (!existingSeatNumbers.isEmpty()) {
            throw new IllegalArgumentException("Seat numbers already exist: " + existingSeatNumbers);
        }

        // O(1) - Batch insert with optimal batch size
        List<Seat> savedSeats = seatRepository.saveAllInBatch(seats);

        // O(1) - Update slot totals once
        updateSlotTotals(slot, totalSeats, totalSeats);

        log.info("Bulk created {} seats for slot {} in optimized manner ({}x{} pattern)", 
                totalSeats, slotId, totalRows, seatsPerRow);

        // O(n) - Convert to response with pre-sized collection
        List<SeatResponse> responses = new ArrayList<>(savedSeats.size());
        for (Seat seat : savedSeats) {
            responses.add(convertToSeatResponse(seat));
        }
        
        return responses;
    }
    
    /**
     * ASYNC VERSION: For very large bulk operations
     * Returns immediately with task ID for status tracking
     */
    public String bulkCreateSeatsAsync(Long slotId, String rowStart, String rowEnd,
                                     int seatsPerRow, double price) {
        // For operations > 1000 seats, delegate to async processor
        String taskId = UUID.randomUUID().toString();
        // Implementation would use @Async and return task tracking
        // This is a placeholder for async implementation
        return taskId;
    }

    /**
     * OPTIMIZED: Get available seats with indexed query
     * Time Complexity: O(n) but with database optimization
     * Database Calls: 2 (slot validation + filtered query)
     */
    public List<SeatResponse> getAvailableSeats(Long slotId) {
        // O(1) - Single database call
        MovieSlot slot = getMovieSlotById(slotId);
        
        // O(n) - Optimized query with index on (slot_id, is_booked)
        List<Seat> availableSeats = seatRepository.findBySlotAndIsBookedOrderBySeatNumber(slot, false);
        
        // O(n) - Efficient conversion with pre-sized collection
        List<SeatResponse> responses = new ArrayList<>(availableSeats.size());
        for (Seat seat : availableSeats) {
            responses.add(convertToSeatResponse(seat));
        }
        
        return responses;
    }

    /**
     * OPTIMIZED: Get booked seats with indexed query
     * Time Complexity: O(n) but with database optimization
     */
    public List<SeatResponse> getBookedSeats(Long slotId) {
        // O(1) - Single database call
        MovieSlot slot = getMovieSlotById(slotId);
        
        // O(n) - Optimized query with index on (slot_id, is_booked)
        List<Seat> bookedSeats = seatRepository.findBySlotAndIsBookedOrderBySeatNumber(slot, true);
        
        // O(n) - Efficient conversion
        return bookedSeats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    public List<SeatResponse> getAllSeatsWithStatus(Long slotId) {
        return getAllSeatsForSlot(slotId);
    }

    public SeatResponse getSeatResponseById(Long seatId) {
        Seat seat = getSeatById(seatId);
        return convertToSeatResponse(seat);
    }

    public SeatAvailabilityResponse checkSeatAvailability(Long seatId) {
        Seat seat = getSeatById(seatId);
        return new SeatAvailabilityResponse(
                seat.getSeatId(),
                seat.getSeatNumber(),
                !seat.isBooked(),
                seat.getPrice()
        );
    }

    /**
     * OPTIMIZED: Get seats by price range with indexed query
     * Time Complexity: O(n) with proper indexing
     * Database Calls: 2 (slot validation + range query)
     */
    public List<SeatResponse> getSeatsByPriceRange(Long slotId, double minPrice, double maxPrice) {
        // Input validation
        if (minPrice < 0 || maxPrice < minPrice) {
            throw new IllegalArgumentException("Invalid price range");
        }
        
        // O(1) - Single database call
        MovieSlot slot = getMovieSlotById(slotId);
        
        // O(n) - Optimized range query with index on (slot_id, price)
        List<Seat> seats = seatRepository.findBySlotAndPriceBetweenOrderByPrice(slot, minPrice, maxPrice);
        
        // O(n) - Efficient conversion
        return seats.stream()
                .map(this::convertToSeatResponse)
                .collect(Collectors.toList());
    }

    /**
     * OPTIMIZED: Get seat availability count without loading entities
     * Time Complexity: O(1) with proper indexing
     */
    public Map<String, Integer> getSeatAvailabilityCounts(Long slotId) {
        // O(1) - Single database call
        MovieSlot slot = getMovieSlotById(slotId);
        
        // O(1) - Count queries with indexes
        int totalSeats = (int) seatRepository.countBySlot(slot);
        int bookedSeats = (int) seatRepository.countBySlotAndIsBooked(slot, true);
        int availableSeats = totalSeats - bookedSeats;
        
        Map<String, Integer> counts = new HashMap<>();
        counts.put("total", totalSeats);
        counts.put("booked", bookedSeats);
        counts.put("available", availableSeats);
        
        return counts;
    }

    // ============ OPTIMIZED HELPER METHODS ============

    /**
     * Get movie slot by ID with caching potential
     * Time Complexity: O(1)
     */
    private MovieSlot getMovieSlotById(Long slotId) {
        return movieSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie slot not found with ID: " + slotId));
    }

    /**
     * Get seat by ID with error handling
     * Time Complexity: O(1)
     */
    private Seat getSeatById(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));
    }

    /**
     * Create seat entity efficiently
     * Time Complexity: O(1)
     */
    private Seat createSeatEntity(MovieSlot slot, SeatRequest request) {
        Seat seat = new Seat();
        seat.setSlot(slot);
        seat.setSeatNumber(request.getSeatNumber());
        seat.setPrice(request.getPrice());
        seat.setBooked(false); // Default to available
        return seat;
    }

    /**
     * Validate seat request with comprehensive checks
     * Time Complexity: O(1)
     */
    private void validateSeatRequest(SeatRequest request) {
        if (request.getSeatNumber() == null || request.getSeatNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Seat number cannot be null or empty");
        }
        if (request.getPrice() < 0) {
            throw new IllegalArgumentException("Seat price cannot be negative");
        }
    }

    /**
     * Validate bulk create parameters
     * Time Complexity: O(1)
     */
    private void validateBulkCreateParameters(String rowStart, String rowEnd, int seatsPerRow, double price) {
        if (rowStart == null || rowStart.length() != 1) {
            throw new IllegalArgumentException("Row start must be a single character");
        }
        if (rowEnd == null || rowEnd.length() != 1) {
            throw new IllegalArgumentException("Row end must be a single character");
        }
        if (seatsPerRow <= 0 || seatsPerRow > 50) {
            throw new IllegalArgumentException("Seats per row must be between 1 and 50");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    /**
     * Calculate row count efficiently
     * Time Complexity: O(1)
     */
    private int calculateRowCount(String rowStart, String rowEnd) {
        char start = rowStart.toUpperCase().charAt(0);
        char end = rowEnd.toUpperCase().charAt(0);
        
        if (start > end) {
            throw new IllegalArgumentException("Row start must be <= row end");
        }
        
        return end - start + 1;
    }

    /**
     * Validate unique seat number with current seat context
     * Time Complexity: O(1)
     */
    private void validateUniqueSeatNumber(Seat currentSeat, String newSeatNumber) {
        Optional<Seat> existingSeat = seatRepository.findBySlotAndSeatNumber(
                currentSeat.getSlot(), newSeatNumber);
        if (existingSeat.isPresent() && !existingSeat.get().getSeatId().equals(currentSeat.getSeatId())) {
            throw new IllegalArgumentException("Seat number already exists: " + newSeatNumber);
        }
    }

    /**
     * Update seat booking status helper
     * Time Complexity: O(1)
     */
    private void updateSeatBookingStatus(Seat seat, boolean newBookingStatus) {
        boolean previousBookingStatus = seat.isBooked();
        if (previousBookingStatus != newBookingStatus) {
            seat.setBooked(newBookingStatus);
            updateAvailableSeatsCount(seat.getSlot(), previousBookingStatus, newBookingStatus);
        }
    }

    /**
     * Update available seats count for slot
     * Time Complexity: O(1)
     */
    private void updateAvailableSeatsCount(MovieSlot slot, boolean previousStatus, boolean newStatus) {
        if (previousStatus && !newStatus) {
            // Seat was booked, now available
            slot.setAvailableSeats(slot.getAvailableSeats() + 1);
        } else if (!previousStatus && newStatus) {
            // Seat was available, now booked
            slot.setAvailableSeats(slot.getAvailableSeats() - 1);
        }
        movieSlotRepository.save(slot);
    }

    /**
     * Update slot totals in batch
     * Time Complexity: O(1)
     */
    private void updateSlotTotals(MovieSlot slot, int totalSeatsChange, int availableSeatsChange) {
        slot.setTotalSeats(slot.getTotalSeats() + totalSeatsChange);
        slot.setAvailableSeats(slot.getAvailableSeats() + availableSeatsChange);
        movieSlotRepository.save(slot);
    }

    /**
     * Convert seat to response DTO efficiently
     * Time Complexity: O(1)
     */
    private SeatResponse convertToSeatResponse(Seat seat) {
        SeatResponse response = new SeatResponse();
        response.setSeatId(seat.getSeatId());
        response.setSeatNumber(seat.getSeatNumber());
        response.setBooked(seat.isBooked());
        response.setPrice(seat.getPrice());
        response.setSlotId(seat.getSlot().getSlotId());
        return response;
    }

    /**
     * HIGHLY OPTIMIZED: Generate seats with pre-sized collections and minimal object creation
     * Time Complexity: O(r × s) where r = rows, s = seats per row
     * Memory Optimization: Pre-sized ArrayList to prevent resizing
     */
    private List<Seat> generateSeatsOptimized(MovieSlot slot, String rowStart, String rowEnd,
                                            int seatsPerRow, double price, int totalSeats) {
        List<Seat> seats = new ArrayList<>(totalSeats); // Pre-sized for efficiency
        char startChar = rowStart.toUpperCase().charAt(0);
        char endChar = rowEnd.toUpperCase().charAt(0);

        // Efficient nested loop with minimal string operations
        for (char row = startChar; row <= endChar; row++) {
            String rowStr = String.valueOf(row); // Create once per row
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                Seat seat = new Seat();
                seat.setSlot(slot);
                seat.setSeatNumber(rowStr + seatNum); // Efficient string concatenation
                seat.setPrice(price);
                seat.setBooked(false);
                seats.add(seat);
            }
        }
        return seats;
    }

    // ============ INNER CLASSES ============

    public static class SeatAvailabilityResponse {
        private Long seatId;
        private String seatNumber;
        private boolean available;
        private double price;

        public SeatAvailabilityResponse(Long seatId, String seatNumber, boolean available, double price) {
            this.seatId = seatId;
            this.seatNumber = seatNumber;
            this.available = available;
            this.price = price;
        }

        public Long getSeatId() {
            return seatId;
        }

        public void setSeatId(Long seatId) {
            this.seatId = seatId;
        }

        public String getSeatNumber() {
            return seatNumber;
        }

        public void setSeatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}
