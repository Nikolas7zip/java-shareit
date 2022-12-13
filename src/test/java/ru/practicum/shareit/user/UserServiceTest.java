package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository mockUserRepository;

    private UserService userService;

    private final UserDto userDto = new UserDto(1L, "Tester", "test@mail.com");
    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(mockUserRepository);
        user = new User();
        user.setId(1L);
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
    }

    @Test
    void shouldCreateUser() {
        when(mockUserRepository.save(any()))
                .thenReturn(user);
        UserDto createdUser = userService.create(userDto);
        assertEquals(userDto, createdUser);
    }

    @Test
    void shouldFindUser() {
        when(mockUserRepository.findById(userDto.getId()))
                .thenReturn(Optional.of(user));
        UserDto findUser = userService.get(userDto.getId());
        assertEquals(userDto, findUser);
    }

    @Test
    void shouldThrowUserNotFound() {
        when(mockUserRepository.findById(2L))
                .thenReturn(Optional.empty());
        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.get(2L));
    }

    @Test
    void shouldFindAllUsers() {
        when(mockUserRepository.findAll())
                .thenReturn(List.of(user));
        List<UserDto> users = userService.getAll();
        assertEquals(List.of(userDto), users);
    }

    @Test
    void shouldUpdateUser() {
        UserDto requestUserDto = new UserDto(1L, "Tester New", "tester_new@mail.com");
        User savedUser = new User();
        savedUser.setId(requestUserDto.getId());
        savedUser.setName(requestUserDto.getName());
        savedUser.setEmail(requestUserDto.getEmail());

        when(mockUserRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(mockUserRepository.save(any()))
                .thenReturn(savedUser);
        UserDto updatedUser = userService.update(requestUserDto);
        assertEquals(requestUserDto, updatedUser);
    }

    @Test
    void shouldUpdateUserWithoutChanges() {
        when(mockUserRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(mockUserRepository.save(any()))
                .thenReturn(user);
        UserDto updatedUser = userService.update(userDto);
        assertEquals(userDto, updatedUser);
    }

    @Test
    void shouldDeleteUser() {
        Mockito.doNothing().when(mockUserRepository).deleteById(anyLong());

        userService.delete(2L);
    }

}
