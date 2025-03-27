package com.mark.client;

import com.mark.domain.Review;
import com.mark.exception.ReviewsClientException;
import com.mark.exception.ReviewsServerException;
import com.mark.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewsRestClient {

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    private final WebClient webClient;

    public Flux<Review> retrieveReviews(String movieId) {
        String url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUriString();

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> handle4xxError(clientResponse, movieId))
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxError)
                .bodyToFlux(Review.class)
                .retryWhen(RetryUtil.retrySpec());
    }

    private Mono<Throwable> handle4xxError(ClientResponse response, String movieId) {
        log.info("Status code is : {}", response.statusCode().value());

        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
            return Mono.empty();
        }

        return response.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new ReviewsClientException(errorBody)));
    }

    private Mono<Throwable> handle5xxError(ClientResponse response) {
        log.info("Status code is : {}", response.statusCode().value());

        return response.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new ReviewsServerException("Server Exception in ReviewsService " + errorBody)));
    }
}
