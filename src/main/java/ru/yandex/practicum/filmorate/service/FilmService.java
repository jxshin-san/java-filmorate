package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.db.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int DEFAULT_POPULAR_COUNT = 10;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       MpaDbStorage mpaStorage,
                       GenreDbStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film create(Film film) {
        validateReleaseDate(film);
        checkMpaExists(film.getMpa());
        checkGenresExist(film.getGenres());
        film.setLikes(new LinkedHashSet<>());
        Film created = filmStorage.add(film);
        log.info("Создан фильм id={}", created.getId());
        return created;
    }

    public Film update(Film film) {
        Film existing = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));

        validateReleaseDate(film);
        checkMpaExists(film.getMpa());
        checkGenresExist(film.getGenres());

        film.setLikes(existing.getLikes());
        Film updated = filmStorage.update(film);
        log.info("Обновлён фильм id={}", updated.getId());
        return updated;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film getFilm(int id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film addLike(int filmId, int userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Film film = getFilm(filmId);
        filmStorage.addLike(filmId, userId);
        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return film;
    }

    public Film removeLike(int filmId, int userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Film film = getFilm(filmId);
        filmStorage.removeLike(filmId, userId);
        film.getLikes().remove(userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
        return film;
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = count == null ? DEFAULT_POPULAR_COUNT : count;
        if (limit <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным");
        }
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(limit)
                .toList();
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза не может быть пустой");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private void checkMpaExists(Mpa mpa) {
        if (mpa == null) {
            return;
        }
        if (mpa.getId() == 0) {
            throw new ValidationException("Рейтинг MPA обязателен");
        }
        mpaStorage.findById(mpa.getId())
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + mpa.getId() + " не найден"));
    }

    private void checkGenresExist(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        for (Genre genre : genres) {
            if (genre == null || genre.getId() == 0) {
                throw new ValidationException("Жанр должен содержать id");
            }
            genreStorage.findById(genre.getId())
                    .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден"));
        }
    }
}