package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateFilm() {
        Map<String, Object> film = Map.of(
                "name", "Интерстеллар",
                "description", "Фильм о космосе",
                "releaseDate", "2014-11-07",
                "duration", 169
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                film,
                String.class
        );

        System.out.println("\nПроверка: создание фильма");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldRejectFilmWithEmptyName() {
        Map<String, Object> film = Map.of(
                "name", "",
                "description", "Фильм о космосе",
                "releaseDate", "2014-11-07",
                "duration", 169
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                film,
                String.class
        );

        System.out.println("\nПроверка: пустое название");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldRejectFilmWithNegativeDuration() {
        Map<String, Object> film = Map.of(
                "name", "Интерстеллар",
                "description", "Фильм о космосе",
                "releaseDate", "2014-11-07",
                "duration", -10
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                film,
                String.class
        );

        System.out.println("\nПроверка: отрицательная продолжительность");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldRejectFilmReleasedBefore1895() {
        Map<String, Object> film = Map.of(
                "name", "Старый фильм",
                "description", "Фильм",
                "releaseDate", "1895-12-27",
                "duration", 100
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                film,
                String.class
        );

        System.out.println("\nПроверка: дата раньше 28.12.1895");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private String url() {
        return "http://localhost:" + port + "/films";
    }

    @Test
    void shouldAcceptFilmReleasedOn28December1895() {
        Map<String, Object> film = Map.of(
                "name", "Первый фильм",
                "description", "Фильм",
                "releaseDate", "1895-12-28",
                "duration", 1
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                film,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldRejectDescriptionLongerThan200Characters() {
        Map<String, Object> film = Map.of(
                "name", "Интерстеллар",
                "description", "a".repeat(201),
                "releaseDate", "2014-11-07",
                "duration", 169
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                film,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldAcceptDescriptionWith200Characters() {
        Map<String, Object> film = Map.of(
                "name", "Интерстеллар",
                "description", "a".repeat(200),
                "releaseDate", "2014-11-07",
                "duration", 169
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                film,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldRejectZeroDuration() {
        Map<String, Object> film = Map.of(
                "name", "Интерстеллар",
                "description", "Фильм о космосе",
                "releaseDate", "2014-11-07",
                "duration", 0
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                film,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldUpdateFilm() {
        Map<String, Object> film = Map.of(
                "name", "Интерстеллар",
                "description", "Фильм о космосе",
                "releaseDate", "2014-11-07",
                "duration", 169
        );

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                url(),
                film,
                Map.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        Assertions.assertNotNull(createResponse.getBody());
        Integer id = (Integer) createResponse.getBody().get("id");

        Map<String, Object> updatedFilm = Map.of(
                "id", id,
                "name", "Интерстеллар UPDATED",
                "description", "Новое описание",
                "releaseDate", "2014-11-07",
                "duration", 180
        );

        ResponseEntity<Map> updateResponse = restTemplate.exchange(
                url(),
                HttpMethod.PUT,
                new HttpEntity<>(updatedFilm),
                Map.class
        );

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        Assertions.assertNotNull(updateResponse.getBody());
        assertEquals(
                "Интерстеллар UPDATED",
                updateResponse.getBody().get("name")
        );
        assertEquals(
                180,
                updateResponse.getBody().get("duration")
        );
    }

    @Test
    void shouldReturn404WhenFilmDoesNotExist() {
        Map<String, Object> film = Map.of(
                "id", 999999,
                "name", "Несуществующий фильм",
                "description", "Описание",
                "releaseDate", "2014-11-07",
                "duration", 100
        );

        ResponseEntity<String> response = restTemplate.exchange(
                url(),
                HttpMethod.PUT,
                new HttpEntity<>(film),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetAllFilms() {
        Map<String, Object> film = Map.of(
                "name", "Интерстеллар",
                "description", "Фильм о космосе",
                "releaseDate", "2014-11-07",
                "duration", 169
        );

        restTemplate.postForEntity(
                url(),
                film,
                String.class
        );

        ResponseEntity<String> response = restTemplate.getForEntity(
                url(),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}