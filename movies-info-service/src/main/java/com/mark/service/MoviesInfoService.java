package com.mark.service;

import com.mark.domain.MovieInfo;
import com.mark.repository.MoviesInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MoviesInfoService {

    private final MoviesInfoRepository moviesInfoRepository;

    public Flux<MovieInfo> getAllMovieInfos() {
        return moviesInfoRepository.findAll();
    }

    public Mono<MovieInfo> getMovieInfoById(String id) {
        return moviesInfoRepository.findById(id);
    }

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return moviesInfoRepository.save(movieInfo);
    }

    public Mono<MovieInfo> updateMovieInfo(String id, MovieInfo movieInfo) {
        return moviesInfoRepository.findById(id)
                .flatMap(mi -> {
                    mi.updateMovieInfo(movieInfo);
                    return moviesInfoRepository.save(mi);
                });
    }

    public Mono<Void> deleteMovieInfo(String id) {
        return moviesInfoRepository.deleteById(id);
    }

    public Flux<MovieInfo> getMovieInfoByYear(Integer year) {
        return moviesInfoRepository.findByYear(year);
    }
}
