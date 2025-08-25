package com.movieDekho.MovieDekho.dtos.booking;

import lombok.Data;

import java.util.List;

@Data
public class TicketRequest {
    private String movie;
    private String showtime;
    private List<String> seats;
    private String customerName;
    private String customerEmail;
    private double totalPrice;
}
