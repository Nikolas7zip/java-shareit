package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> getById(Long id);

    Optional<User> getByEmail(String email);

    List<User> getAll();

    User add(User user);

    User update(User user);

    void delete(Long id);
}
