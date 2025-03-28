package com.mark.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    void sink_replay() {
        // given
        Sinks.Many<Integer> replaySink = Sinks.many().replay().latest();

        // when
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // then
        Flux<Integer> integerFlux_first = replaySink.asFlux();
        integerFlux_first.subscribe(i -> System.out.println("subscriber_first: " + i));

        Flux<Integer> integerFlux_second = replaySink.asFlux();
        integerFlux_second.subscribe(i -> System.out.println("subscriber_second: " + i));

        replaySink.tryEmitNext(3);

        Flux<Integer> integerFlux_third = replaySink.asFlux();
        integerFlux_third.subscribe(i -> System.out.println("integerFlux_third: " + i));
    }

    @Test
    void sinks_multicast() {
        // given
        Sinks.Many<Integer> multicast = Sinks.many().multicast().onBackpressureBuffer();

        // when
        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // then
        Flux<Integer> integerFlux_first = multicast.asFlux();
        integerFlux_first.subscribe(i -> System.out.println("subscriber_first: " + i));

        Flux<Integer> integerFlux_second = multicast.asFlux();
        integerFlux_second.subscribe(i -> System.out.println("subscriber_second: " + i));

        multicast.tryEmitNext(3);
    }

    @Test
    void sinks_unicast() {
        // given
        Sinks.Many<Integer> unicast = Sinks.many().unicast().onBackpressureBuffer();

        // when
        unicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        unicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // then
        Flux<Integer> integerFlux_first = unicast.asFlux();
        integerFlux_first.subscribe(i -> System.out.println("subscriber_first: " + i));

        Flux<Integer> integerFlux_second = unicast.asFlux();
        integerFlux_second.subscribe(i -> System.out.println("subscriber_second: " + i));

        unicast.tryEmitNext(3);
    }
}
