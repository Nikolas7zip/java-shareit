package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> findById(Long id);

    List<Item> findByOwner(Long userId);

    Item add(Item item);

    Item update(Item item);

    List<Item> findAvailableToRentByText(String text);
}
