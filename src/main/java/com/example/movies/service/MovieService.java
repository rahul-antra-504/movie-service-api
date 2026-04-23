package com.example.movies.service;

import com.example.movies.dto.Movie;
import com.example.movies.dto.MovieResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

@Service
public class MovieService {

    private final WebClient webClient;

    private final ExecutorService executor;

    public MovieService(WebClient.Builder webClientBuilder, @Value("${movie.api.base-url}") String baseUrl, ExecutorService executor) {
        this.executor = executor;
        this.webClient = webClientBuilder.baseUrl( baseUrl).build();
    }

    public MovieResponse getMoviesWithParams(String title, Integer year, Integer page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParamIfPresent("Title", Optional.ofNullable(title))
                        .queryParamIfPresent("Year", Optional.ofNullable(year))
                        .queryParamIfPresent("page", Optional.ofNullable(page))
                        .build())
                .retrieve()
                .bodyToMono(MovieResponse.class)
                .block();
    }

    public List<Movie> getAllMoviesMultithreaded() {
        MovieResponse firstPage = getMoviesWithParams(null, null, 1);
        int totalPages = firstPage.getTotalPages();

        List<Movie> allMovies = new CopyOnWriteArrayList<>(firstPage.getData());
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 2; i <= totalPages; i++) {
            final int currentPage = i;
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                return getMoviesWithParams(null, null, currentPage);
            }, executor).thenAccept(response -> {
                if (response != null && response.getData() != null) {
                    allMovies.addAll(response.getData());
                }
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return allMovies;
    }
}
