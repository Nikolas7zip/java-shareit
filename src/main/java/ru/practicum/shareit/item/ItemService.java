package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentOutput;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto get(Long userId, Long id);

    List<ItemDto> getByOwner(Long userId);

    List<ItemDto> getAvailableToRentByText(Long userId, String text);

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, ItemDto itemDto);

    CommentOutput createComment(Long userId, Long itemId, CommentDto commentDto);
}
