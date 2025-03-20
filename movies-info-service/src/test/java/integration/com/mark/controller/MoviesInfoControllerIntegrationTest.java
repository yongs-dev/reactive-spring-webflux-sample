package com.mark.controller;

import com.mark.domain.MovieInfo;
import com.mark.repository.MoviesInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntegrationTest {

    private static final String TARGET_ID = "KNIGHT";
    private static final String MOVIES_INFO_URL = "/v1/movieInfos";

    @Autowired
    MoviesInfoRepository repository;

    @Autowired
    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        List<MovieInfo> movieInfos = List.of(
                MovieInfo.builder().name("Batman Begins").year(2005).casts(List.of("Christian Bale", "Michael Cane")).release_date(LocalDate.parse("2005-06-15")).build(),
                MovieInfo.builder().name("The Dark Knight").year(2008).casts(List.of("Christian Bale", "HeathLedger")).release_date(LocalDate.parse("2008-07-18")).build(),
                MovieInfo.builder().movieInfoId(TARGET_ID).name("Dark Knight Rises").year(2012).casts(List.of("Christian Bale", "Tom Hardy")).release_date(LocalDate.parse("2012-07-20")).build()
        );

        repository.saveAll(movieInfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll().block();
    }

    @Test
    void getAllMovieInfos() {
        webClient.get().uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        webClient.get().uri(MOVIES_INFO_URL + "/{id}", TARGET_ID)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(response -> {
                    MovieInfo movieInfo = response.getResponseBody();
                    assertNotNull(movieInfo);
                    assertEquals(TARGET_ID, movieInfo.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo() {
        MovieInfo movieInfo = MovieInfo.builder().movieInfoId(TARGET_ID).name("Dark Knight Rises").year(2012).casts(List.of("Christian Bale", "Tom Hardy")).release_date(LocalDate.parse("2012-07-20")).build();

        webClient.post().uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(response -> {
                    MovieInfo savedMovieInfo = response.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                });
    }

    @Test
    void updateMovieInfo() {
        MovieInfo movieInfo = MovieInfo.builder().movieInfoId(TARGET_ID).name("Dark Knight Rises").year(2025).casts(List.of("Christian Bale", "Tom Hardy")).release_date(LocalDate.parse("2025-07-20")).build();

        webClient.put().uri(MOVIES_INFO_URL + "/{id}", movieInfo.getMovieInfoId())
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(response -> {
                    MovieInfo updatedMovieInfo = response.getResponseBody();
                    assert updatedMovieInfo != null;
                    assert updatedMovieInfo.getMovieInfoId() != null;
                    assert updatedMovieInfo.getYear() == 2025;
                    assert updatedMovieInfo.getRelease_date().equals(movieInfo.getRelease_date());
                });
    }

    @Test
    void deleteMovieInfo() {
        webClient.delete().uri(MOVIES_INFO_URL + "/{id}", TARGET_ID)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}