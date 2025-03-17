package com.mark.service;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public static void main(String[] args) {
        FluxAndMonoGeneratorService service = new FluxAndMonoGeneratorService();
        service.namesFlux()
                .subscribe(System.out::println);

        service.nameMono()
                .subscribe(System.out::println);
    }

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .log();
    }

    public Mono<String> nameMono() {
        return Mono.just("alex")
                .log();
    }

    public Flux<String> namesFlux_map() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .log();
    }

    public Flux<String> namesFlux_map(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(s -> s.length() > stringLength)
                .map(s -> {
                    s = s.toUpperCase();
                    s = s.length() + "-" + s;
                    return s;
                })
                .log();
    }

    public Flux<String> namesFlux_immutability() {
        Flux<String> names = Flux.fromIterable(List.of("alex", "ben", "chloe"));
        names.map(String::toUpperCase);
        return names;
    }

    public Flux<String> namesFlux_flatMap(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(s -> s.length() > stringLength)
                .map(String::toUpperCase)
                .flatMap(this::splitString)
                .log();
    }

    public Flux<String> namesFlux_flatMap_async(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(s -> s.length() > stringLength)
                .map(String::toUpperCase)
                .flatMap(this::splitString_withDelay)
                .log();
    }

    public Flux<String> namesFlux_concatMap(int stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(s -> s.length() > stringLength)
                .map(String::toUpperCase)
                .concatMap(this::splitString_withDelay)
                .log();
    }

    public Flux<String> splitString(String name) {
        return Flux.fromArray(name.split(""));
    }

    public Flux<String> splitString_withDelay(String name) {
        int delay = new Random().nextInt(1000);

        return Flux.fromArray(name.split(""))
                .delayElements(Duration.ofMillis(delay));
    }

    public Mono<List<String>> namesMono_flatMap(int stringLength) {
        return Mono.just("alex")
                .filter(s -> s.length() > stringLength)
                .map(String::toUpperCase)
                .flatMap(this::splitStringMono)
                .log();
    }

    public Flux<String> namesMono_flatMapMany(int stringLength) {
        return Mono.just("alex")
                .filter(s -> s.length() > stringLength)
                .map(String::toUpperCase)
                .flatMapMany(this::splitString)
                .log();
    }

    private Mono<List<String>> splitStringMono(String name) {
        return Mono.just(List.of(name.split("")));
    }

    public Flux<String> namesFlux_transform(int stringLength) {
        Function<Flux<String>, Publisher<String>> filterMap = name -> name
                .filter(s -> s.length() > stringLength)
                .map(String::toUpperCase)
                .flatMap(this::splitString);

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filterMap)
                .log();
    }

    public Flux<String> namesFlux_transform_with_defaultIfEmpty(int stringLength) {
        Function<Flux<String>, Publisher<String>> filterMap = name -> name
                .filter(s -> s.length() > stringLength)
                .map(String::toUpperCase)
                .flatMap(this::splitString);

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filterMap)
                .defaultIfEmpty("defaultIfEmpty")
                .log();
    }

    public Flux<String> namesFlux_transform_with_switchIfEmpty(int stringLength) {
        Function<Flux<String>, Publisher<String>> filterMap = name -> name
                .filter(s -> s.length() > stringLength)
                .map(String::toUpperCase)
                .flatMap(this::splitString);

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filterMap)
                .switchIfEmpty(
                        Flux.just("switchIfEmpty")
                                .transform(filterMap)
                )
                .log();
    }

    public Flux<String> explore_concat() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");

        return Flux.concat(abcFlux, defFlux)
                .log();
    }

    public Flux<String> explore_concatWith() {
        Mono<String> aMono = Mono.just("A");
        Flux<String> defFlux = Flux.just("D", "E", "F");

        return aMono.concatWith(defFlux)
                .log();
    }

    public Flux<String> explore_merge() {
        Flux<String> abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        Flux<String> defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.merge(abcFlux, defFlux)
                .log();
    }

    public Flux<String> explore_mergeWith_flux() {
        Flux<String> abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        Flux<String> defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return abcFlux.mergeWith(defFlux)
                .log();
    }

    public Flux<String> explore_mergeWith_mono() {
        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");

        return aMono.mergeWith(bMono)
                .log();
    }

    public Flux<String> explore_mergeSequential() {
        Flux<String> abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        Flux<String> defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.mergeSequential(abcFlux, defFlux)
                .log();
    }

    public Flux<String> explore_zip() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");

        return Flux.zip(abcFlux, defFlux, (first, second) -> first + second)
                .log();
    }

    public Flux<String> explore_zip_1() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");
        Flux<String> _123Flux = Flux.just("1", "2", "3");
        Flux<String> _456Flux = Flux.just("4", "5", "6");

        return Flux.zip(abcFlux, defFlux, _123Flux, _456Flux)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
                .log();
    }

    public Flux<String> explore_zipWith_flux() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");

        return abcFlux.zipWith(defFlux, (first, second) -> first + second)
                .log();
    }

    public Mono<String> explore_zipWith_mono() {
        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");

        return aMono.zipWith(bMono)
                .map(t2 -> t2.getT1() + t2.getT2())
                .log();
    }
}
