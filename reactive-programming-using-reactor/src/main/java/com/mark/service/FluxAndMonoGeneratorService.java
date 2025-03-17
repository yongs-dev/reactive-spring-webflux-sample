package com.mark.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class FluxAndMonoGeneratorService {

    public static void main(String[] args) {
        FluxAndMonoGeneratorService service = new FluxAndMonoGeneratorService();
        service.namesFlux()
                .subscribe(System.out::println);

        service.nameMono()
                .subscribe(System.out::println);
    }

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe")).log();
    }

    public Mono<String> nameMono() {
        return Mono.just("alex").log();
    }
}
