package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> getById(Long id);

    List<Item> getOwnerItems(Long userId);

    Item add(Item item);

    Item update(Item item);

    List<Item> findItemsToRentByText(String text);
}
