package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto getItem(Long userId, Long id);

    List<ItemDto> getOwnerItems(Long userId);

    List<ItemDto> findItemsToRentByText(Long userId, String text);

    ItemDto createNewItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, ItemDto itemDto);
}
