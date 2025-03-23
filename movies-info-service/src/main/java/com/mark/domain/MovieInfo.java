package com.mark.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Document
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MovieInfo {

    @Id
    private final String movieInfoId;

    @NotBlank(message = "movieInfo.name must be present")
    private String name;

    @NotNull
    @Positive(message = "movieInfo.year must be a Positive value")
    private Integer year;

    @NotEmpty(message = "movieInfo.casts must be present")
    private List<@NotBlank(message = "movieInfo.cast must be present") String> casts;
    private LocalDate release_date;

    public void updateYear(int year) {
        this.year = year;
    }

    public void updateMovieInfo(MovieInfo movieInfo) {
        this.name = movieInfo.getName();
        this.year = movieInfo.getYear();
        this.casts = movieInfo.getCasts();
        this.release_date = movieInfo.getRelease_date();
    }
}
