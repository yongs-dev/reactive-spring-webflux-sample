package com.mark.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.mark.domain.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebClient
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "restClient.moviesInfoUrl=http://localhost:${wiremock.server.port}/v1/movieinfos",
        "restClient.reviewsUrl=http://localhost:${wiremock.server.port}/v1/reviews"
})
public class MoviesControllerIntegrationTest {

    private static final String MOVIE_ID = "TEST";
    private static final String MOVIES_URL = "/v1/movies";
    private static final String MOVIE_INFOS_URL = "/v1/movieinfos";
    private static final String REVIEWS_URL = "/v1/reviews";

    @Autowired
    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        WireMock.reset();
    }

    @Test
    void retrieveMovieById() {
        // given
        stubFor(
                get(urlEqualTo(MOVIE_INFOS_URL + "/" + MOVIE_ID))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK)
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")
                        )
        );

        stubFor(
                get(urlPathEqualTo(REVIEWS_URL))
                        .withQueryParam("movieInfoId", equalTo(MOVIE_ID))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK)
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")
                        )
        );

        // when
        webClient.get().uri(MOVIES_URL + "/{id}", MOVIE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });

        // then
    }

    @Test
    void retrieveMovieById_404() {
        // given
        stubFor(
                get(urlEqualTo(MOVIE_INFOS_URL + "/" + MOVIE_ID))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.NOT_FOUND)
                        )
        );

        stubFor(
                get(urlPathEqualTo(REVIEWS_URL))
                        .withQueryParam("movieInfoId", equalTo(MOVIE_ID))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK)
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")
                        )
        );

        // when
        webClient.get().uri(MOVIES_URL + "/{id}", MOVIE_ID)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("MovieInfo not found for id: " + MOVIE_ID);

        WireMock.verify(1, getRequestedFor(urlEqualTo(MOVIE_INFOS_URL + "/" + MOVIE_ID)));
    }

    @Test
    void retrieveMovieById_reviews_404() {
        // given
        stubFor(
                get(urlEqualTo(MOVIE_INFOS_URL + "/" + MOVIE_ID))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK)
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")
                        )
        );

        stubFor(
                get(urlPathEqualTo(REVIEWS_URL))
                        .withQueryParam("movieInfoId", equalTo(MOVIE_ID))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.NOT_FOUND)
                        )
        );

        // when
        webClient.get().uri(MOVIES_URL + "/{id}", MOVIE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().isEmpty();
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });

    }

    @Test
    void retrieveMovieById_5XX() {
        // given
        stubFor(
                get(urlEqualTo(MOVIE_INFOS_URL + "/" + MOVIE_ID))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .withBody("MovieInfo Service Unavailable")
                        )
        );

        // when
        webClient.get().uri(MOVIES_URL + "/{id}", MOVIE_ID)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in MoviesInfoService MovieInfo Service Unavailable");

        WireMock.verify(4, getRequestedFor(urlEqualTo(MOVIE_INFOS_URL + "/" + MOVIE_ID)));
    }

    @Test
    void retrieveMovieById_reviews_5XX() {
        // given
        stubFor(
                get(urlEqualTo(MOVIE_INFOS_URL + "/" + MOVIE_ID))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK)
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")
                        )
        );

        stubFor(
                get(urlPathEqualTo(REVIEWS_URL))
                        .withQueryParam("movieInfoId", equalTo(MOVIE_ID))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .withBody("Review Service Unavailable")
                        )
        );

        // when
        webClient.get().uri(MOVIES_URL + "/{id}", MOVIE_ID)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in ReviewsService Review Service Unavailable");

        WireMock.verify(4, getRequestedFor(urlPathMatching(REVIEWS_URL + "/*")));
    }
}
