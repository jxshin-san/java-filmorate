package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "m.id as mpa_id, m.name as mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        loadGenresForFilms(films);
        loadLikesForFilms(films);
        return films;
    }

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setObject(3, film.getReleaseDate());
            stmt.setInt(4, film.getDuration());
            stmt.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return stmt;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            film.setId(keyHolder.getKey().intValue());
        }

        updateFilmGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        updateFilmGenres(film);
        return film;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Film> findById(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "m.id as mpa_id, m.name as mpa_name " +
                "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        loadGenresForFilms(films);
        loadLikesForFilms(films);
        return Optional.of(films.get(0));
    }

    private void updateFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = "MERGE INTO film_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, film.getGenres(), film.getGenres().size(),
                (ps, genre) -> {
                    ps.setInt(1, film.getId());
                    ps.setInt(2, genre.getId());
                });
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        String inSql = films.stream().map(f -> "?").collect(Collectors.joining(","));
        String sql = "SELECT fg.film_id, g.id, g.name " +
                "FROM film_genres fg JOIN genres g ON g.id = fg.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ")";

        Map<Integer, Set<Genre>> genresByFilm = new HashMap<>();

        jdbcTemplate.query(sql, films.stream().map(Film::getId).toArray(), rs -> {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            genresByFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        });

        for (Film film : films) {
            film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }
    }

    private void loadLikesForFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        String inSql = films.stream().map(f -> "?").collect(Collectors.joining(","));
        String sql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + inSql + ")";

        Map<Integer, Set<Integer>> likesByFilm = new HashMap<>();

        jdbcTemplate.query(sql, films.stream().map(Film::getId).toArray(), rs -> {
            int filmId = rs.getInt("film_id");
            likesByFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                    .add(rs.getInt("user_id"));
        });

        for (Film film : films) {
            film.setLikes(likesByFilm.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        LocalDate releaseDate = rs.getDate("release_date") != null ? rs.getDate("release_date").toLocalDate() : null;
        film.setReleaseDate(releaseDate);
        film.setDuration(rs.getInt("duration"));

        int mpaId = rs.getInt("mpa_id");
        if (!rs.wasNull()) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }
        return film;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "MERGE INTO film_likes (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }
}