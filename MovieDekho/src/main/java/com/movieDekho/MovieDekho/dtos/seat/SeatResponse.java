    package com.movieDekho.MovieDekho.dtos.seat;

    import lombok.Data;

    @Data
    public class SeatResponse {
        private Long seatId;
        private String seatNumber;
        private boolean isBooked;
        private double price;
        private Long slotId;
    }