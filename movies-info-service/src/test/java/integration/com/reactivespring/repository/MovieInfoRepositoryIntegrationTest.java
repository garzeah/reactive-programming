package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    void findAll() {
        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findAll();

        StepVerifier
            .create(moviesInfoFlux)
            .expectNextCount(3);
    }

    @Test
    void findById() {

        Mono<MovieInfo> movieInfo = movieInfoRepository.findById("abc");

        StepVerifier.create(movieInfo)
            .assertNext(movieInfo1 -> {
                assertEquals("Dark Knight Rises", movieInfo1.getName());
            });
    }

    @Test
    void saveMovieInfo() {

        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins1",2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        Mono<MovieInfo> savedMovieInfo = movieInfoRepository.save(movieInfo);

        StepVerifier.create(savedMovieInfo)
            .assertNext(movieInfo1 -> {
                assertNotNull(movieInfo1.getMovieInfoId());
            });

    }

    @Test
    void updateMovieInfo() {

        MovieInfo movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2021);

        Mono<MovieInfo> savedMovieInfo = movieInfoRepository.save(movieInfo);

        StepVerifier.create(savedMovieInfo)
            .assertNext(movieInfo1 -> {
                assertNotNull(movieInfo1.getMovieInfoId());
                assertEquals(2021, movieInfo1.getYear());
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
}