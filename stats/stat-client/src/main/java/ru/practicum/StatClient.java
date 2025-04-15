package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class StatClient {
    private final RestClient restClient;
    private final String baseUrl;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatClient(@Value("${stat-server.url}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void createHit(EndpointHitDto hitDto) {
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/hit")
                .build()
                .toUriString();
        log.info("POST to URI: {}", uri);

        restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(hitDto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique)
                .queryParam("uris", uris)
                .toUriString();

        log.info("GET request to URI: {}", uri);

        try {
            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new RuntimeException("Client error: " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new RuntimeException("Server error: " + res.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            log.error("Error while getting stats", e);
            return List.of();
        }
    }
}
