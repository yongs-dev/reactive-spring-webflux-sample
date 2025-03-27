package com.mark.controller;


import com.mark.client.MoviesInfoRestClient;
import com.mark.client.ReviewsRestClient;
import com.mark.domain.Movie;
import com.mark.domain.MovieInfo;
import com.mark.domain.Review;
import com.mark.exception.MoviesInfoClientException;
import com.mark.exception.MoviesInfoServerException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesController.class)
@AutoConfigureWebTestClient
public class MoviesControllerUnitTest {

    @MockBean
    private MoviesInfoRestClient moviesInfoRestClient;

    @MockBean
    private ReviewsRestClient reviewsRestClient;

    @Autowired
    private WebTestClient webClient;

    private List<Review> initReviews() {
        return List.of(
                Review.builder().movieInfoId(1L).comment("Awesome Movie").rating(9.0).build(),
                Review.builder().movieInfoId(1L).comment("Awesome Movie1").rating(9.0).build(),
                Review.builder().movieInfoId(2L).comment("Excellent Movie").rating(8.0).build()
        );
    }


    @Test
    void retrieveMovieById() {
        List<Review> reviewList = initReviews();

        String movieId = "abc";

        when(moviesInfoRestClient.retrieveMovieInfo(anyString()))
                .thenReturn(Mono.just(MovieInfo.builder().movieInfoId(movieId).name("Batman Begins").year(2005).cast(List.of("Christian Bale", "Michael Cane")).release_date(LocalDate.parse("2005-06-15")).build()));

        when(reviewsRestClient.retrieveReviews(anyString()))
                .thenReturn(Flux.fromIterable(reviewList));

        //when
        webClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                            var movie = movieEntityExchangeResult.getResponseBody();
                            assert Objects.requireNonNull(movie).getReviewList().size() == 3;
                            assertEquals("Batman Begins", movie.getMovieInfo().getName());
                        }
                );
        //then
    }

    @Test
    void retrieveMovieById_404() {
        List<Review> reviewList = initReviews();

        when(moviesInfoRestClient.retrieveMovieInfo(anyString()))
                .thenReturn(Mono.error(new MoviesInfoClientException("MovieNotFound", 404)));

        when(reviewsRestClient.retrieveReviews(anyString()))
                .thenReturn(Flux.fromIterable(reviewList));

        //when
        webClient.get()
                .uri("/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(String.class)
                .consumeWith(movieEntityExchangeResult -> {
                            var errorMessage = movieEntityExchangeResult.getResponseBody();
                            assertEquals("MovieNotFound", errorMessage);
                        }
                );
    }

    @Test
    void retrieveMovieById_500() {
        List<Review> reviewList = initReviews();

        String errorMsg = "Service Unavailable";
        when(moviesInfoRestClient.retrieveMovieInfo(anyString()))
                .thenReturn(Mono.error(new MoviesInfoServerException(errorMsg)));

        when(reviewsRestClient.retrieveReviews(anyString()))
                .thenReturn(Flux.fromIterable(reviewList));

        //when
        webClient.get()
                .uri("/v1/movies/{id}", "abc")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .consumeWith(movieEntityExchangeResult -> {
                            var errorMessage = movieEntityExchangeResult.getResponseBody();
                            assertEquals(errorMsg, errorMessage);
                        }
                );
    }
}
