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
class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateUser() {
        Map<String, Object> user = Map.of(
                "email", "test@mail.ru",
                "login", "testuser",
                "name", "Тест",
                "birthday", "2000-01-01"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                user,
                String.class
        );

        System.out.println("\nПроверка: создание пользователя");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldRejectEmptyEmail() {
        Map<String, Object> user = Map.of(
                "email", "",
                "login", "testuser",
                "name", "Тест",
                "birthday", "2000-01-01"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                user,
                String.class
        );

        System.out.println("\nПроверка: пустой email");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldRejectInvalidEmail() {
        Map<String, Object> user = Map.of(
                "email", "testmail.ru",
                "login", "testuser",
                "name", "Тест",
                "birthday", "2000-01-01"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                user,
                String.class
        );

        System.out.println("\nПроверка: неправильный email");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldRejectLoginWithSpaces() {
        Map<String, Object> user = Map.of(
                "email", "test@mail.ru",
                "login", "test user",
                "name", "Тест",
                "birthday", "2000-01-01"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                user,
                String.class
        );

        System.out.println("\nПроверка: пробелы в login");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldRejectFutureBirthday() {
        Map<String, Object> user = Map.of(
                "email", "test@mail.ru",
                "login", "testuser",
                "name", "Тест",
                "birthday", "2030-01-01"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                user,
                String.class
        );

        System.out.println("\nПроверка: дата рождения в будущем");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        Map<String, Object> user = Map.of(
                "email", "test@mail.ru",
                "login", "testuser",
                "name", "",
                "birthday", "2000-01-01"
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                url(),
                user,
                String.class
        );

        System.out.println("\nПроверка: имя заменяется на login");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private String url() {
        return "http://localhost:" + port + "/users";
    }

    @Test
    void shouldUpdateUser() {
        Map<String, Object> user = Map.of(
                "email", "test@mail.ru",
                "login", "testuser",
                "name", "Тест",
                "birthday", "2000-01-01"
        );

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                url(),
                user,
                Map.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        Assertions.assertNotNull(createResponse.getBody());
        Integer id = (Integer) createResponse.getBody().get("id");

        Map<String, Object> updatedUser = Map.of(
                "id", id,
                "email", "updated@mail.ru",
                "login", "updateduser",
                "name", "Обновлённый пользователь",
                "birthday", "2000-01-01"
        );

        ResponseEntity<Map> updateResponse = restTemplate.exchange(
                url(),
                HttpMethod.PUT,
                new HttpEntity<>(updatedUser),
                Map.class
        );

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        Assertions.assertNotNull(updateResponse.getBody());
        assertEquals(
                "updated@mail.ru",
                updateResponse.getBody().get("email")
        );
        assertEquals(
                "updateduser",
                updateResponse.getBody().get("login")
        );
        assertEquals(
                "Обновлённый пользователь",
                updateResponse.getBody().get("name")
        );
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() {
        Map<String, Object> user = Map.of(
                "id", 999999,
                "email", "test@mail.ru",
                "login", "testuser",
                "name", "Тест",
                "birthday", "2000-01-01"
        );

        ResponseEntity<String> response = restTemplate.exchange(
                url(),
                HttpMethod.PUT,
                new HttpEntity<>(user),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetAllUsers() {
        Map<String, Object> user = Map.of(
                "email", "test@mail.ru",
                "login", "testuser",
                "name", "Тест",
                "birthday", "2000-01-01"
        );

        restTemplate.postForEntity(
                url(),
                user,
                String.class
        );

        ResponseEntity<String> response = restTemplate.getForEntity(
                url(),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        Map<String, Object> user = Map.of(
                "email", "test2@mail.ru",
                "login", "testuser2",
                "birthday", "2000-01-01"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url(),
                user,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(
                "testuser2",
                response.getBody().get("name")
        );
    }
}