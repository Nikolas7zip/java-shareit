package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Item name should not be blank or null")
    private String name;

    @NotBlank(message = "Item description should not be blank or null")
    private String description;

    @NotNull(message = "Item available should not be null")
    private Boolean available;
}
