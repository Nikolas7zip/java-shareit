package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;


import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto get(Long id) {
        User user = userRepository.findById(id)
                                  .orElseThrow(() -> new EntityNotFoundException(User.class, id));

        return UserMapper.mapToUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        List<User> users = userRepository.findAll();

        return UserMapper.mapToUserDto(users);
    }

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        User user = userRepository.save(UserMapper.mapToUser(userDto));
        log.info("Created " + user);

        return UserMapper.mapToUserDto(user);
    }

    @Transactional
    @Override
    public UserDto update(UserDto userDto) {
        UserDto databaseUserDto = get(userDto.getId());

        if (userDto.getName() != null) {
            databaseUserDto.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            databaseUserDto.setEmail(userDto.getEmail());
        }

        User user = userRepository.save(UserMapper.mapToUser(databaseUserDto));
        log.info("Updated " + user);

        return UserMapper.mapToUserDto(user);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
        log.info("Deleted user id=" + id);
    }
}
