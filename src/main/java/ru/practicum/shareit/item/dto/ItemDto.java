package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShort;
import ru.practicum.shareit.item.comment.CommentOutput;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@NoArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Item name should not be blank or null")
    private String name;

    @NotBlank(message = "Item description should not be blank or null")
    private String description;

    @NotNull(message = "Item available should not be null")
    private Boolean available;

    private Long requestId;

    private BookingShort lastBooking;

    private BookingShort nextBooking;

    private List<CommentOutput> comments;

    public ItemDto(Long id, String name, String description, Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
    }
}
