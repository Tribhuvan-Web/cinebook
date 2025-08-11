package com.movieDekho.MovieDekho.dtos.booking;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SeatSelectionResponse {
    private Long slotId;
    private List<String> seatNumbers;
    private Double totalAmount;
    private String movieTitle;
    private String movieDescription;
    private String cinemaName;
    private String screenName;
    private String showTime;
    private String showDate;
    private List<SeatDetails> seatDetails;

    @Data
    @NoArgsConstructor
    public static class SeatDetails {
        private String seatNumber;
        private String seatType;
        private Double price;
    }
}
