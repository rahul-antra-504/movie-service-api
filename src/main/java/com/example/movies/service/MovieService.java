package com.example.movies.service;

import com.example.movies.dto.Movie;
import com.example.movies.dto.MovieResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MovieService {
    private final WebClient webClient;

    public MovieService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://jsonmock.hackerrank.com/api/moviesdata/search/").build();
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
        int totalPages = firstPage.getTotal_pages();

        List<Movie> allMovies = new CopyOnWriteArrayList<>(firstPage.getData());
        ExecutorService executor = Executors.newFixedThreadPool(10); // 10 parallel threads
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
        executor.shutdown();

        return allMovies;
    }
}
