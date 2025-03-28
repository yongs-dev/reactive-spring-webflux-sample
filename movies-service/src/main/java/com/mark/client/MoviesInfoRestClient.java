package com.mark.client;

import com.mark.domain.MovieInfo;
import com.mark.exception.MoviesInfoClientException;
import com.mark.exception.MoviesInfoServerException;
import com.mark.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MoviesInfoRestClient {

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    private final WebClient webClient;

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        return webClient
                .get()
                .uri(moviesInfoUrl + "/{id}", movieId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> handle4xxError(clientResponse, movieId))
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxError)
                .bodyToMono(MovieInfo.class)
//                .retry(3);
                .retryWhen(RetryUtil.retrySpec());
    }

    private Mono<Throwable> handle4xxError(ClientResponse response, String movieId) {
        log.info("Status code is : {}", response.statusCode().value());

        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
            return Mono.error(new MoviesInfoClientException("MovieInfo not found for id: " + movieId, response.statusCode().value()));
        }

        return response.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new MoviesInfoClientException(errorBody, response.statusCode().value())));
    }

    private Mono<Throwable> handle5xxError(ClientResponse response) {
        log.info("Status code is : {}", response.statusCode().value());

        return response.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new MoviesInfoServerException("Server Exception in MoviesInfoService " + errorBody)));
    }

    public Flux<MovieInfo> retrieveMovieInfoStream() {
        return webClient
                .get()
                .uri(moviesInfoUrl + "/stream")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new MoviesInfoClientException(errorBody, response.statusCode().value()))))
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxError)
                .bodyToFlux(MovieInfo.class)
                .retryWhen(RetryUtil.retrySpec());
    }
}
