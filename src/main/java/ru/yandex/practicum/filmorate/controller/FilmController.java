package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final List<Film> films = new ArrayList<>();
    private int nextId = 1;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateReleaseDate(film);

        film.setId(nextId++);
        films.add(film);

        log.info("Создан фильм: {}", film);

        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        validateReleaseDate(film);

        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getId() == film.getId()) {
                films.set(i, film);

                log.info("Обновлён фильм: {}", film);

                return film;
            }
        }

        log.warn("Попытка обновить несуществующий фильм с id: {}", film.getId());

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильм с id " + film.getId() + " не найден");
    }

    @GetMapping
    public List<Film> findAll() {
        return films;
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null) {
            log.warn("Ошибка валидации фильма: дата релиза отсутствует");

            throw new ValidationException(
                    "Дата релиза не может быть пустой"
            );
        }

        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка валидации фильма: дата релиза {} раньше допустимой", film.getReleaseDate());

            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года" );
        }
    }
}