package com.mark.router;

import com.mark.domain.Review;
import com.mark.exception.ReviewNotFoundException;
import com.mark.exceptionhandler.GlobalErrorHandler;
import com.mark.handler.ReviewHandler;
import com.mark.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewRouterUnitTest {

    private static final String TARGET_ID = "ID";
    private static final String REVIEW_URL = "/v1/reviews";

    @MockBean
    private ReviewReactiveRepository reviewRepositoryMock;

    @Autowired
    private WebTestClient webClient;

    @Test
    void getAllReviews() {
        List<Review> reviews = List.of(
                Review.builder().movieInfoId(1L).comment("Awesome Movie").rating(9.0).build(),
                Review.builder().movieInfoId(1L).comment("Wonderful Movie").rating(9.0).build(),
                Review.builder().reviewId(TARGET_ID).movieInfoId(2L).comment("Excellent Movie").rating(8.0).build()
        );

        when(reviewRepositoryMock.findAll()).thenReturn(Flux.fromIterable(reviews));

        webClient.get().uri(REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void addReview() {
        // given
        Review review = Review.builder().reviewId(TARGET_ID).movieInfoId(1L).comment("Awesome Movie").rating(9.0).build();

        when(reviewRepositoryMock.save(isA(Review.class))).thenReturn(Mono.just(review));

        // when
        webClient.post().uri(REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(response -> {
                    Review savedReview = response.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });

        // then
    }

    @Test
    void addReview_validation() {
        // given
        Review review = Review.builder().reviewId(TARGET_ID).movieInfoId(null).comment("Awesome Movie").rating(-9.0).build();

        when(reviewRepositoryMock.save(isA(Review.class))).thenReturn(Mono.just(review));

        // when
        webClient.post().uri(REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId : must not be null,rating.negative : rating is negative and please pass a non-negative value");

        // then
    }

    @Test
    void updateReview() {
        // given
        Review review = Review.builder().reviewId(TARGET_ID).movieInfoId(1L).comment("Awesome Movie").rating(9.0).build();
        when(reviewRepositoryMock.findById((String) any())).thenReturn(Mono.just(review));
        when(reviewRepositoryMock.save(isA(Review.class))).thenReturn(Mono.just(Review.builder().reviewId(TARGET_ID).movieInfoId(1L).comment("G.O.A.T Movie").rating(10.0).build()));

        // when
        webClient.put().uri(REVIEW_URL + "/{id}", review.getReviewId())
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(response -> {
                    Review updatedReview = response.getResponseBody();
                    assert updatedReview != null;
                    assertEquals(TARGET_ID, updatedReview.getReviewId());
                    assertEquals("G.O.A.T Movie", updatedReview.getComment());
                    assertEquals(10.0, updatedReview.getRating());
                });

        // then
    }

    @Test
    void updateReview_validation() {
        // given
        Review review = Review.builder().reviewId("?????").movieInfoId(1L).comment("Awesome Movie").rating(9.0).build();
        String errorMessage = "Review not found for the given Review id ?????";

        when(reviewRepositoryMock.findById((String) any())).thenReturn(Mono.error(new ReviewNotFoundException(errorMessage)));
        when(reviewRepositoryMock.save(isA(Review.class))).thenReturn(Mono.just(Review.builder().reviewId(TARGET_ID).movieInfoId(1L).comment("G.O.A.T Movie").rating(10.0).build()));

        // when
        webClient.put().uri(REVIEW_URL + "/{id}", review.getReviewId())
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo(errorMessage);

        // then
    }

    @Test
    void deleteReview() {
        // given
        Review review = Review.builder().reviewId(TARGET_ID).movieInfoId(1L).comment("Awesome Movie").rating(9.0).build();

        when(reviewRepositoryMock.findById((String) any())).thenReturn(Mono.just(review));
        when(reviewRepositoryMock.deleteById(TARGET_ID)).thenReturn(Mono.empty());

        // when
        webClient.delete().uri(REVIEW_URL + "/{id}", TARGET_ID)
                .exchange()
                .expectStatus()
                .isNoContent();

        // then
    }
}
