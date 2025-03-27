package com.mark.controller;

import com.mark.client.MoviesInfoRestClient;
import com.mark.client.ReviewsRestClient;
import com.mark.domain.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
public class MoviesController {

    private final MoviesInfoRestClient moviesInfoRestClient;
    private final ReviewsRestClient reviewsRestClient;

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String movieId) {
        return moviesInfoRestClient.retrieveMovieInfo(movieId)
                .flatMap(movieInfo ->
                        reviewsRestClient.retrieveReviews(movieId)
                                .collectList()
                                .map(reviews -> Movie.builder().movieInfo(movieInfo).reviewList(reviews).build())
                );
    }
}
