package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentOutput;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.pagination.EntityPagination;

import java.util.List;

public interface ItemService {
    ItemDto get(Long userId, Long id);

    List<ItemDto> getByOwner(Long userId, EntityPagination pagination);

    List<ItemDto> getAvailableToRentByText(Long userId, String text, EntityPagination pagination);

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, ItemDto itemDto);

    CommentOutput createComment(Long userId, Long itemId, CommentDto commentDto);
}
