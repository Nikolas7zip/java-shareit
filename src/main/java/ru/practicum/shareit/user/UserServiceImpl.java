package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto getUser(Long id) {
        Optional<User> userOptional = userRepository.getById(id);
        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException("User id=" + id + " not found");
        }

        return UserMapper.mapToUserDto(userOptional.get());
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.getAll();

        return UserMapper.mapToUserDto(users);
    }

    @Override
    public UserDto createNewUser(UserDto userDto) {
        throwIfEmailDuplicate(userDto.getEmail());
        User user = userRepository.add(UserMapper.mapToUser(userDto));
        log.info("Created " + user);

        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        UserDto databaseUserDto = getUser(userDto.getId());
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
    public void deleteUser(Long id) {
        getUser(id);
        userRepository.delete(id);
        log.info("Deleted user id=" + id);
    }

    private void throwIfEmailDuplicate(String email) {
        if (userRepository.getByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Duplicate email " + email);
        }
    }
}
