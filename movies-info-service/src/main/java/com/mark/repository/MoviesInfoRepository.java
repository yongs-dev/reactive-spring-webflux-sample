package com.mark.repository;

import com.mark.domain.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MoviesInfoRepository extends ReactiveMongoRepository<MovieInfo, String> {

}
