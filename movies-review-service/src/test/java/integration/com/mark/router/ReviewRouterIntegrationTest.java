package com.mark.router;

import com.mark.domain.Review;
import com.mark.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReviewRouterIntegrationTest {

    private static final String REVIEW_URL = "/v1/reviews";
    private static final String TARGET_ID = "ID";

    @Autowired
    ReviewReactiveRepository reviewRepository;

    @Autowired
    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        List<Review> reviews = List.of(
                Review.builder().movieInfoId(1L).comment("Awesome Movie").rating(9.0).build(),
                Review.builder().movieInfoId(1L).comment("Wonderful Movie").rating(9.0).build(),
                Review.builder().reviewId(TARGET_ID).movieInfoId(2L).comment("Excellent Movie").rating(8.0).build()
        );

        reviewRepository.saveAll(reviews).blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll().block();
    }

    @Test
    void getAllReviews() {
        webClient.get().uri(REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void getReviewsByMovieInfoId() {
        webClient.get().uri(UriComponentsBuilder.fromUriString(REVIEW_URL).queryParam("movieInfoId", 1L).buildAndExpand().toUri())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void addReview() {
        // given
        Review review = Review.builder().reviewId(null).movieInfoId(1L).comment("Awesome Movie").rating(9.0).build();

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
    void updateReview() {
        // given
        Review review = Review.builder().reviewId(TARGET_ID).movieInfoId(1L).comment("G.O.A.T Movie").rating(10.0).build();

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
    void deleteReview() {

        // when
        webClient.delete().uri(REVIEW_URL + "/{id}", TARGET_ID)
                .exchange()
                .expectStatus()
                .isNoContent();

        // then
    }
}
