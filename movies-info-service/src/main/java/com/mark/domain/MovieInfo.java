package com.mark.domain;

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
    private String name;
    private Integer year;
    private List<String> casts;
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
