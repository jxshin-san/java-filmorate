package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidationTest {

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
    void shouldRejectEmptyEmail() {
        User user = createValidUser();
        user.setEmail("");

        Set<?> violations = validator.validate(user);

        System.out.println("\nПроверка: пустой email");
        System.out.println("email: \"" + user.getEmail() + "\"");
        System.out.println("Ошибки: " + violations);

        assertTrue(!violations.isEmpty(),
                "Пустой email должен быть запрещён");
    }

    @Test
    void shouldRejectInvalidEmail() {
        User user = createValidUser();
        user.setEmail("testmail.ru");

        Set<?> violations = validator.validate(user);

        System.out.println("\nПроверка: неправильный email");
        System.out.println("email: " + user.getEmail());
        System.out.println("Ошибки: " + violations);

        assertTrue(!violations.isEmpty(),
                "Неправильный email должен быть запрещён");
    }

    @Test
    void shouldAcceptValidEmail() {
        User user = createValidUser();
        user.setEmail("test@mail.ru");

        Set<?> violations = validator.validate(user);

        System.out.println("\nПроверка: правильный email");
        System.out.println("email: " + user.getEmail());
        System.out.println("Ошибки: " + violations);

        assertTrue(violations.isEmpty(),
                "Правильный email должен проходить");
    }

    @Test
    void shouldRejectEmptyLogin() {
        User user = createValidUser();
        user.setLogin("");

        Set<?> violations = validator.validate(user);

        System.out.println("\nПроверка: пустой login");
        System.out.println("login: \"" + user.getLogin() + "\"");
        System.out.println("Ошибки: " + violations);

        assertTrue(!violations.isEmpty(),
                "Пустой login должен быть запрещён");
    }

    @Test
    void shouldAcceptLoginWithNoSpaces() {
        User user = createValidUser();
        user.setLogin("testuser");

        Set<?> violations = validator.validate(user);

        System.out.println("\nПроверка: login без пробелов");
        System.out.println("login: " + user.getLogin());
        System.out.println("Ошибки: " + violations);

        assertTrue(violations.isEmpty(),
                "Login без пробелов должен проходить");
    }

    private User createValidUser() {
        User user = new User();

        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Тест");
        user.setBirthday(java.time.LocalDate.of(2000, 1, 1));

        return user;
    }

    @Test
    void shouldRejectNullEmail() {
        User user = createValidUser();
        user.setEmail(null);

        Set<?> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Null email должен быть запрещён");
    }

    @Test
    void shouldRejectNullLogin() {
        User user = createValidUser();
        user.setLogin(null);

        Set<?> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Null login должен быть запрещён");
    }
}
