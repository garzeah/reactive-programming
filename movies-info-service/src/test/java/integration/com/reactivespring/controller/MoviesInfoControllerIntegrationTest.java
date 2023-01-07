package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MoviesInfoControllerIntegrationTest {
    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    static String MOVIES_INFO_URL = "/v1/movieinfos";;

    @BeforeEach
    void setUp() {
        List<MovieInfo> movieInfo = List.of(
                new MovieInfo(null, "Batman Begins",2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );

        movieInfoRepository
                .saveAll(movieInfo)
                .blockLast(); // Need to make sure this operation gets completed before running test cases
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {
        MovieInfo movieInfo =  new MovieInfo(null, "Batman Begins 2",2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
            .post()
            .uri(MOVIES_INFO_URL)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {
                MovieInfo savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                assert savedMovieInfo != null;
                assert savedMovieInfo.getMovieInfoId() != null;
            });
    }

    @Test
    void getAllMoviesInfos() {
        webTestClient
            .get()
            .uri(MOVIES_INFO_URL)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(MovieInfo.class)
            .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        String movieInfoId = "abc";

        webTestClient
            .get()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Dark Knight Rises");
//            .expectBody(MovieInfo.class)
//            .consumeWith(movieInfoEntityExchangeResult -> {
//                MovieInfo movieInfo = movieInfoEntityExchangeResult.getResponseBody();
//                assertNotNull(movieInfo);
//            });
    }

    @Test
    void updateMovieInfo() {
        String movieInfoId = "abc";
        MovieInfo movieInfo =  new MovieInfo(null, "Dark Knight Rises 2",
        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
            .put()
            .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {
                MovieInfo updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                assert updatedMovieInfo != null;
                assert updatedMovieInfo.getMovieInfoId() != null;
                assertEquals("Dark Knight Rises 2", updatedMovieInfo.getName());
            });
    }

    @Test
    void deleteMovieInfo() {
        movieInfoRepository.deleteById("abc").block();
        Flux<MovieInfo> movieInfos = movieInfoRepository.findAll();

        StepVerifier.create(movieInfos)
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void deleteMovieInfoById() {
        String id = "abc";

        webTestClient
            .delete()
            .uri(MOVIES_INFO_URL + "/{id}", id)
            .exchange()
            .expectStatus()
            .isNoContent();
    }
}