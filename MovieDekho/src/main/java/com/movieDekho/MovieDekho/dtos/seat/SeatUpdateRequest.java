package com.movieDekho.MovieDekho.dtos.seat;

import lombok.Data;

@Data
public class SeatUpdateRequest {
    private String seatNumber;
    private double price;
    private Boolean booked;
}
