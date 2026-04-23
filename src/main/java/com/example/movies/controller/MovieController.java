package com.example.movies.controller;

import com.example.movies.dto.Movie;
import com.example.movies.dto.MovieResponse;
import com.example.movies.service.MovieService;
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
    public List<Movie> getAllMovies() {
        return movieService.getAllMoviesMultithreaded();
    }

    @GetMapping(value = "/movies", params = {"title"})
    public MovieResponse getMoviesByQuery(
            @RequestParam("title") String title,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "page", required = false) Integer page) {
        return movieService.getMoviesWithParams(title, year, page);
    }
}
