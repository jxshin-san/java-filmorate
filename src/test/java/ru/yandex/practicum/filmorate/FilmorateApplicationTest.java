package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldRejectEmptyName() {
        Film film = createValidFilm();
        film.setName("");

        Set<?> violations = validator.validate(film);

        System.out.println("\nПроверка: пустое название");
        System.out.println("name: \"" + film.getName() + "\"");
        System.out.println("Ошибки: " + violations);

        assertEquals(1, violations.size(),
                "Пустое название фильма должно быть запрещено");
    }

    @Test
    void shouldRejectDescriptionLongerThan200Characters() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));

        Set<?> violations = validator.validate(film);

        System.out.println("\nПроверка: описание 201 символ");
        System.out.println("Длина description: " + film.getDescription().length());
        System.out.println("Ошибки: " + violations);

        assertEquals(1, violations.size(),
                "Описание длиннее 200 символов должно быть запрещено");
    }

    @Test
    void shouldAcceptDescriptionWith200Characters() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));

        Set<?> violations = validator.validate(film);

        System.out.println("\nПроверка: описание 200 символов");
        System.out.println("Длина description: " + film.getDescription().length());
        System.out.println("Ошибки: " + violations);

        assertTrue(violations.isEmpty(),
                "Описание длиной ровно 200 символов должно проходить");
    }

    @Test
    void shouldRejectZeroDuration() {
        Film film = createValidFilm();
        film.setDuration(0);

        Set<?> violations = validator.validate(film);

        System.out.println("\nПроверка: duration = 0");
        System.out.println("duration: " + film.getDuration());
        System.out.println("Ошибки: " + violations);

        assertEquals(1, violations.size(),
                "Продолжительность 0 должна быть запрещена");
    }

    @Test
    void shouldRejectNegativeDuration() {
        Film film = createValidFilm();
        film.setDuration(-1);

        Set<?> violations = validator.validate(film);

        System.out.println("\nПроверка: duration < 0");
        System.out.println("duration: " + film.getDuration());
        System.out.println("Ошибки: " + violations);

        assertEquals(1, violations.size(),
                "Отрицательная продолжительность должна быть запрещена");
    }

    @Test
    void shouldAcceptPositiveDuration() {
        Film film = createValidFilm();
        film.setDuration(100);

        Set<?> violations = validator.validate(film);

        System.out.println("\nПроверка: положительный duration");
        System.out.println("duration: " + film.getDuration());
        System.out.println("Ошибки: " + violations);

        assertTrue(violations.isEmpty(),
                "Положительная продолжительность должна проходить");
    }

    private Film createValidFilm() {
        Film film = new Film();

        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм о космосе");
        film.setDuration(169);

        return film;
    }

    @Test
    void shouldRejectNullName() {
        Film film = createValidFilm();
        film.setName(null);

        Set<?> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Null name должен быть запрещён");
    }
}