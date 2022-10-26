package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(message = "User name should not be blank or null")
    private String name;

    @Email(message = "User email does not match the pattern")
    @NotBlank(message = "User email should not be blank or null")
    private String email;
}
