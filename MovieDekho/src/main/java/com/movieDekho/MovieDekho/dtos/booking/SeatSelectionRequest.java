package com.movieDekho.MovieDekho.dtos.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SeatSelectionRequest {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<String> seatNumbers;
}
