package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class User {
    private int id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String login;

    private String name;
    private LocalDate birthday;
    private Set<Integer> friends = new LinkedHashSet<>();
}
