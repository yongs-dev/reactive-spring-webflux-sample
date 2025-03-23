package com.mark.repository;

import com.mark.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MoviesInfoRepositoryIntegrationTest {

    private static final String TARGET_ID = "KNIGHT";

    @Autowired
    MoviesInfoRepository repository;

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
    void findAll() {
        // given

        // when
        Flux<MovieInfo> movieInfosFlux = repository.findAll().log();

        // then
        StepVerifier.create(movieInfosFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        // given

        // when
        Mono<MovieInfo> movieInfoMono = repository.findById(TARGET_ID).log();

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> assertEquals("Dark Knight Rises", movieInfo.getName()))
                .verifyComplete();
    }

    @Test
    void findByYear() {
        // given

        // when
        Flux<MovieInfo> movieInfoMono = repository.findByYear(2012).log();

        // then
        StepVerifier.create(movieInfoMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByName() {
        // given

        // when
        Mono<MovieInfo> movieInfoMono = repository.findByName("Dark Knight Rises").log();

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> assertEquals("Dark Knight Rises", movieInfo.getName()))
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        // given
        MovieInfo movieInfo = MovieInfo.builder().name("Batman Begins").year(2005).casts(List.of("Christian Bale", "Michael Cane")).release_date(LocalDate.parse("2005-06-15")).build();

        // when
        Mono<MovieInfo> movieInfoMono = repository.save(movieInfo).log();

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(mi -> {
                    assertNotNull(mi.getMovieInfoId());
                    assertEquals("Batman Begins", mi.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        // given
        MovieInfo movieInfo = repository.findById(TARGET_ID).block();
        movieInfo.updateYear(2025);

        // when
        Mono<MovieInfo> movieInfoMono = repository.save(movieInfo).log();

        // then
        StepVerifier.create(movieInfoMono)
                .assertNext(mi -> {
                    assertEquals(2025, mi.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        // given

        // when
        repository.deleteById(TARGET_ID).block();
        Flux<MovieInfo> movieInfosFlux = repository.findAll().log();

        // then
        StepVerifier.create(movieInfosFlux)
                .expectNextCount(2)
                .verifyComplete();
    }
}