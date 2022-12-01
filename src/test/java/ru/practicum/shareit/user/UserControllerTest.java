package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private final UserDto userDto = new UserDto(1L, "Tester", "test@mail.com");

    @Test
    void shouldCreateUser() throws Exception {
        when(userService.create(any()))
                .thenReturn(userDto);

        mockMvc.perform(post("/users")
                            .content(mapper.writeValueAsString(userDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void shouldReturnBadRequestWhenCreateUserWithFailEmail() throws Exception {
        UserDto userFailEmail = new UserDto(1L, "FailTester", "tester@");
        when(userService.create(any()))
                .thenReturn(null);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userFailEmail))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateUserWithBlankName() throws Exception {
        UserDto userFailName = new UserDto(1L, " ", "tester@mail.com");
        when(userService.create(any()))
                .thenReturn(null);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userFailName))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindUser() throws Exception {
        when(userService.get(anyLong()))
                .thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", userDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void shouldReturnNotFoundUser() throws Exception {
        when(userService.get(anyLong()))
                .thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/users/{userId}", userDto.getId()))
                .andExpect(status().isNotFound());

    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserDto updatedUserDto = new UserDto(1L, "Tester", "test_new@mail.com");
        String json = "{ \"email\" : \"test_new@mail.com\"}";

        when(userService.update(any()))
                .thenReturn(updatedUserDto);

        mockMvc.perform(patch("/users/{userId}", updatedUserDto.getId())
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedUserDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedUserDto.getName())))
                .andExpect(jsonPath("$.email", is(updatedUserDto.getEmail())));
    }

    @Test
    void shouldFindAllUsers() throws Exception {
        UserDto secondUserDto = new UserDto(2L, "Admin", "admin@mail.com");
        List<UserDto> userDtos = new ArrayList<>();
        userDtos.add(userDto);
        userDtos.add(secondUserDto);

        when(userService.getAll())
                .thenReturn(userDtos);

        mockMvc.perform(get("/users")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())))
                .andExpect(jsonPath("$[1].id", is(secondUserDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(secondUserDto.getName())))
                .andExpect(jsonPath("$[1].email", is(secondUserDto.getEmail())));

    }

    @Test
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).delete(anyLong());

        mockMvc.perform(delete("/users/{userId}", userDto.getId()))
                .andExpect(status().isOk());
    }
}
