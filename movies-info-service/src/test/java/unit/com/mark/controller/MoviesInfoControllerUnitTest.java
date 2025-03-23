package com.mark.controller;

import com.mark.domain.MovieInfo;
import com.mark.service.MoviesInfoService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;


@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    private static final String TARGET_ID = "KNIGHT";
    private static final String MOVIES_INFO_URL = "/v1/movieInfos";

    @Autowired
    WebTestClient webClient;

    @MockBean
    private MoviesInfoService serviceMock;

    @Test
    void getAllMoviesInfo() {
        List<MovieInfo> movieInfos = List.of(
                MovieInfo.builder().name("Batman Begins").year(2005).casts(List.of("Christian Bale", "Michael Cane")).release_date(LocalDate.parse("2005-06-15")).build(),
                MovieInfo.builder().name("The Dark Knight").year(2008).casts(List.of("Christian Bale", "HeathLedger")).release_date(LocalDate.parse("2008-07-18")).build(),
                MovieInfo.builder().movieInfoId(TARGET_ID).name("Dark Knight Rises").year(2012).casts(List.of("Christian Bale", "Tom Hardy")).release_date(LocalDate.parse("2012-07-20")).build()
        );

        when(serviceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfos));

        webClient.get().uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        MovieInfo movieInfo = MovieInfo.builder().movieInfoId(TARGET_ID).name("Dark Knight Rises").year(2012).casts(List.of("Christian Bale", "Tom Hardy")).release_date(LocalDate.parse("2012-07-20")).build();

        when(serviceMock.getMovieInfoById(TARGET_ID)).thenReturn(Mono.just(movieInfo));

        webClient.get().uri(MOVIES_INFO_URL + "/{id}", TARGET_ID)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(response -> {
                    MovieInfo mi = response.getResponseBody();
                    assertNotNull(mi);
                    assertEquals(TARGET_ID, mi.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo() {
        MovieInfo movieInfo = MovieInfo.builder().movieInfoId(TARGET_ID).name("Dark Knight Rises").year(2012).casts(List.of("Christian Bale", "Tom Hardy")).release_date(LocalDate.parse("2012-07-20")).build();

        when(serviceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

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
                    assertEquals(TARGET_ID, savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo_validation() {
        MovieInfo movieInfo = MovieInfo.builder().movieInfoId(TARGET_ID).name("Dark Knight Rises").year(2012).casts(List.of("")).release_date(LocalDate.parse("2012-07-20")).build();

        when(serviceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

        webClient.post().uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assert responseBody != null;
                });
    }

    @Test
    void updateMovieInfo() {
        MovieInfo movieInfo = MovieInfo.builder().movieInfoId(TARGET_ID).name("Dark Knight Rises").year(2025).casts(List.of("Christian Bale", "Tom Hardy")).release_date(LocalDate.parse("2025-07-20")).build();

        when(serviceMock.updateMovieInfo(isA(String.class), isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

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
        when(serviceMock.deleteMovieInfo(isA(String.class))).thenReturn(Mono.empty());

        webClient.delete().uri(MOVIES_INFO_URL + "/{id}", TARGET_ID)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
