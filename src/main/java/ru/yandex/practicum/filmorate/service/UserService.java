package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend(int userId, int friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);
        if (userId == friendId) {
            throw new IllegalArgumentException("Пользователь не может добавить в друзья самого себя");
        }
        user.getFriends().add((long) friendId);
        friend.getFriends().add((long) userId);
        log.info("Пользователи {} и {} добавлены в друзья", userId, friendId);
        return user;
    }

    public User removeFriend(int userId, int friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);
        user.getFriends().remove((long) friendId);
        friend.getFriends().remove((long) userId);
        log.info("Пользователи {} и {} удалены из друзей", userId, friendId);
        return user;
    }

    public List<User> getFriends(int userId) {
        User user = getUser(userId);
        return user.getFriends().stream()
                .map(id -> getUser(Math.toIntExact(id)))
                .toList();
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUser(userId);
        User other = getUser(otherId);
        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(id -> getUser(Math.toIntExact(id)))
                .toList();
    }

    public User getUser(int id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }
}
