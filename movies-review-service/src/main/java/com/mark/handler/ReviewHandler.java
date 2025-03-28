package com.mark.handler;

import com.mark.domain.Review;
import com.mark.exception.ReviewDataException;
import com.mark.exception.ReviewNotFoundException;
import com.mark.repository.ReviewReactiveRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewHandler {

    private final Validator validator;
    private final ReviewReactiveRepository reviewRepository;
    private final Sinks.Many<Review> reviewsSink = Sinks.many().replay().latest();

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        return request.queryParam("movieInfoId")
                .map(s -> ServerResponse.ok().body(reviewRepository.findByMovieInfoId(Long.parseLong(s)), Review.class))
                .orElseGet(() -> ServerResponse.ok().body(reviewRepository.findAll(), Review.class));
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewRepository::save)
                .doOnNext(reviewsSink::tryEmitNext)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> constraintViolations = validator.validate(review);
        log.info("ConstraintViolations: {}", constraintViolations);

        if (!constraintViolations.isEmpty()) {
            String errorMessage = constraintViolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));

            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        String reviewId = request.pathVariable("id");

        return reviewRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id " + reviewId)))
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.updateEvaluation(reqReview);
                            return review;
                        })
                        .flatMap(reviewRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        String id = request.pathVariable("id");

        return reviewRepository.findById(id)
                .flatMap(review -> reviewRepository.deleteById(id))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewsSink.asFlux(), Review.class);
    }
}
