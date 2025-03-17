package com.mark.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService service = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {
        // given

        // when
        Flux<String> namesFlux = service.namesFlux();

        // then
        StepVerifier.create(namesFlux)
//                .expectNext("alex", "ben", "chloe")
//                .expectNextCount(3)
                .expectNext("alex")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void namesFlux_map() {
        // given

        // when
        Flux<String> namesFlux = service.namesFlux_map();

        // then
        StepVerifier.create(namesFlux)
                .expectNext("ALEX", "BEN", "CHLOE")
                .verifyComplete();
    }

    @Test
    void namesFlux_map_using_filter() {
        // given
        int stringLength = 3;

        // when
        Flux<String> namesFlux = service.namesFlux_map(stringLength);

        // then
        StepVerifier.create(namesFlux)
                .expectNext("4-ALEX", "5-CHLOE")
                .verifyComplete();
    }

    @Test
    void namesFlux_immutability() {
        // given

        // when
        Flux<String> namesFlux = service.namesFlux_immutability();

        // then
        StepVerifier.create(namesFlux)
                // Immutable
                .expectNext("alex", "ben", "chloe")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatMap() {
        // given
        int stringLength = 3;

        // when
        Flux<String> namesFlux = service.namesFlux_flatMap(stringLength);

        // then
        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatMap_async() {
        // given
        int stringLength = 3;

        // when
        Flux<String> namesFlux = service.namesFlux_flatMap_async(stringLength);

        // then
        StepVerifier.create(namesFlux)
                // [FAIL] asynchronous
//                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void namesFlux_concatMap() {
        // given
        int stringLength = 3;

        // when
        Flux<String> namesFlux = service.namesFlux_concatMap(stringLength);

        // then
        StepVerifier.create(namesFlux)
                // concatMap == synchronous
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesMono_flatMap() {
        // given
        int stringLength = 3;

        // when
        Mono<List<String>> value = service.namesMono_flatMap(stringLength);

        // then
        StepVerifier.create(value)
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();
    }

    @Test
    void namesMono_flatMapMany() {
        // given
        int stringLength = 3;

        // when
        Flux<String> namesFlux = service.namesMono_flatMapMany(stringLength);

        // then
        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform() {
        // given
        int stringLength = 3;

        // when
        Flux<String> namesFlux = service.namesFlux_transform(stringLength);

        // then
        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform_with_defaultIfEmpty() {
        // given
        int stringLength = 6;

        // when
        Flux<String> namesFlux = service.namesFlux_transform_with_defaultIfEmpty(stringLength);

        // then
        StepVerifier.create(namesFlux)
                .expectNext("defaultIfEmpty")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform_with_switchIfEmpty() {
        // given
        int stringLength = 6;

        // when
        Flux<String> namesFlux = service.namesFlux_transform_with_switchIfEmpty(stringLength);

        // then
        StepVerifier.create(namesFlux)
                .expectNext("S", "W", "I", "T", "C", "H", "I", "F", "E", "M", "P", "T", "Y")
                .verifyComplete();
    }

    @Test
    void explore_concat() {
        // given

        // when
        Flux<String> concatFlux = service.explore_concat();

        // then
        StepVerifier.create(concatFlux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_concatWith() {
        // given

        // when
        Flux<String> concatWithFlux = service.explore_concatWith();

        // then
        StepVerifier.create(concatWithFlux)
                .expectNext("A", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_merge() {
        // given

        // when
        Flux<String> mergeFlux = service.explore_merge();

        // then
        StepVerifier.create(mergeFlux)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }

    @Test
    void explore_mergeWith() {
        // given

        // when
        Flux<String> mergeWithFlux = service.explore_mergeWith_flux();
        Flux<String> mergeWithMono = service.explore_mergeWith_mono();

        // then
        StepVerifier.create(mergeWithFlux)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();

        StepVerifier.create(mergeWithMono)
                .expectNext("A", "B")
                .verifyComplete();
    }

    @Test
    void explore_mergeSequential() {
        // given

        // when
        Flux<String> mergeFlux = service.explore_mergeSequential();

        // then
        StepVerifier.create(mergeFlux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_zip() {
        // given

        // when
        Flux<String> mergeFlux = service.explore_zip();
        Flux<String> mergeFlux_1 = service.explore_zip_1();


        // then
        StepVerifier.create(mergeFlux)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();

        StepVerifier.create(mergeFlux_1)
                .expectNext("AD14", "BE25", "CF36")
                .verifyComplete();
    }

    @Test
    void explore_zipWith() {
        // given

        // when
        Flux<String> zipWithFlux = service.explore_zipWith_flux();
        Mono<String> zipWithMono = service.explore_zipWith_mono();

        // then
        StepVerifier.create(zipWithFlux)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();

        StepVerifier.create(zipWithMono)
                .expectNext("AB")
                .verifyComplete();
    }
}