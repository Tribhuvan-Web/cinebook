package com.movieDekho.MovieDekho.dtos.movie;

import com.movieDekho.MovieDekho.models.AvailableMovie;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MovieResponseDTO {
    private Long id;
    private String title;
    private LocalDate releaseDate;
    private String duration;
    private String description;
    private String certification;
    private String thumbnail;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<MovieSlotResponseDTO> slots;

    public static MovieResponseDTO fromMovie(AvailableMovie movie) {
        MovieResponseDTO dto = new MovieResponseDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setDuration(movie.getDuration());
        dto.setDescription(movie.getDescription());
        dto.setCertification(movie.getCertification());
        dto.setThumbnail(movie.getThumbnail());
        dto.setStartDate(movie.getStartDate());
        dto.setEndDate(movie.getEndDate());
        dto.setSlots(movie.getSlots().stream()
                .map(MovieSlotResponseDTO::fromMovieSlot)
                .collect(Collectors.toList()));
        return dto;
    }
}
