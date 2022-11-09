package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto get(Long userId, Long id);

    List<ItemDto> getByOwner(Long userId);

    List<ItemDto> getAvailableToRentByText(Long userId, String text);

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, ItemDto itemDto);
}
