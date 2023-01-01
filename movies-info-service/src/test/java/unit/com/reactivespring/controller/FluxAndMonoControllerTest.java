package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient // Web Test Client instance is automatically injected
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void fluxApproach1() {
        webTestClient
            .get()
            .uri("/flux")
            .exchange() // Invokes the endpoint
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(Integer.class)
            .hasSize(3);
    }

    @Test
    void fluxApproach2() {
        Flux<Integer> flux = webTestClient
            .get()
            .uri("/flux")
            .exchange() // Invokes the endpoint
            .expectStatus()
            .is2xxSuccessful()
            .returnResult(Integer.class)
            .getResponseBody();

        StepVerifier
            .create(flux)
            .expectNext(1, 2, 3)
            .verifyComplete();
    }

    @Test
    void fluxApproach3() {
    webTestClient
        .get()
        .uri("/flux")
        .exchange() // Invokes the endpoint
        .expectStatus()
        .is2xxSuccessful()
        .expectBodyList(Integer.class)
        .consumeWith(
            listEntityExchangeResult -> {
              List<Integer> responseBody = listEntityExchangeResult.getResponseBody();
              assert (Objects.requireNonNull(responseBody).size() == 3);
            });
    }

    @Test
    void mono() {
        webTestClient
            .get()
            .uri("/mono")
            .exchange() // Invokes the endpoint
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(String.class)
            .consumeWith(
                stringEntityExchangeResult -> {
                    String responseBody = stringEntityExchangeResult.getResponseBody();
                    assertEquals("Hello World", responseBody);
                });
    }

    @Test
    void stream() {
        Flux<Long> flux = webTestClient
            .get()
            .uri("/stream")
            .exchange() // Invokes the endpoint
            .expectStatus()
            .is2xxSuccessful()
            .returnResult(Long.class)
            .getResponseBody();

        StepVerifier
            .create(flux)
            .expectNext(0L, 1L, 2L, 3L)
            .thenCancel() // Cancels stream
            .verify();
    }
}