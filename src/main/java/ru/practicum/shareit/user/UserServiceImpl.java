package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto get(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("User id=" + id + " not found")
        );

        return UserMapper.mapToUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        List<User> users = userRepository.findAll();

        return UserMapper.mapToUserDto(users);
    }

    @Override
    public UserDto create(UserDto userDto) {
        throwIfEmailDuplicate(userDto.getEmail());
        User user = userRepository.add(UserMapper.mapToUser(userDto));
        log.info("Created " + user);

        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserDto update(UserDto userDto) {
        UserDto databaseUserDto = get(userDto.getId());
        if (userDto.getName() != null) {
            databaseUserDto.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            throwIfEmailDuplicate(userDto.getEmail());
            databaseUserDto.setEmail(userDto.getEmail());
        }

        User user = userRepository.update(UserMapper.mapToUser(databaseUserDto));
        log.info("Updated " + user);

        return UserMapper.mapToUserDto(user);
    }

    @Override
    public void delete(Long id) {
        get(id);
        userRepository.delete(id);
        log.info("Deleted user id=" + id);
    }

    private void throwIfEmailDuplicate(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Duplicate email " + email);
        }
    }
}
