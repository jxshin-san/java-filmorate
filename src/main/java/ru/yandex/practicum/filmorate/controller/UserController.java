package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();
    private int nextId = 1;

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(nextId++);
        users.add(user);

        log.info("Создан пользователь: {}", user);

        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == user.getId()) {
                users.set(i, user);

                log.info("Обновлён пользователь: {}", user);

                return user;
            }
        }

        log.warn("Попытка обновить несуществующего пользователя с id: {}", user.getId());

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с id " + user.getId() + " не найден");
    }

    @GetMapping
    public List<User> findAll() {
        return users;
    }

    private void validateUser(User user) {
        if (user.getLogin().contains(" ")) {
            log.warn(
                    "Ошибка валидации пользователя: логин содержит пробелы: {}",
                    user.getLogin()
            );

            throw new ValidationException(
                    "Логин не должен содержать пробелы"
            );
        }

        if (user.getBirthday() != null &&
                user.getBirthday().isAfter(LocalDate.now())) {

            log.warn(
                    "Ошибка валидации пользователя: дата рождения {} в будущем",
                    user.getBirthday()
            );

            throw new ValidationException(
                    "Дата рождения не может быть в будущем"
            );
        }
    }
}