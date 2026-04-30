package com.example.movies.controller;

import com.example.movies.dto.Movie;
import com.example.movies.dto.MovieResponse;
import com.example.movies.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping(value = "/movies")
    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> movies = movieService.getAllMoviesMultithreaded();
        if (movies == null || movies.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(movies);
    }

    @GetMapping(value = "/movies", params = {"title"})
    public ResponseEntity<MovieResponse> getMoviesByQuery(
            @RequestParam("title") String title,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "page", required = false) Integer page) {
        MovieResponse response =  movieService.getMoviesWithParams(title, year, page);

        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
