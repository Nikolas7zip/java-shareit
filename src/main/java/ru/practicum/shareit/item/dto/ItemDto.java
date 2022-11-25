package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShort;
import ru.practicum.shareit.item.comment.CommentOutput;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Item name should not be blank or null")
    private String name;

    @NotBlank(message = "Item description should not be blank or null")
    private String description;

    @NotNull(message = "Item available should not be null")
    private Boolean available;

    private BookingShort lastBooking;

    private BookingShort nextBooking;

    private List<CommentOutput> comments;
}
