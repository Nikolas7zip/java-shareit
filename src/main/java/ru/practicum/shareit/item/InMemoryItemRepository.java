package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();

    private Long databaseId = 0L;

    @Override
    public Optional<Item> getById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> getOwnerItems(Long userId) {
        return items.values()
                .stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item add(Item item) {
        databaseId++;
        item.setId(databaseId);
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public List<Item> findItemsToRentByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        } else {
            return items.values()
                    .stream()
                    .filter(item -> {
                        String name = item.getName().toLowerCase();
                        String description = item.getDescription().toLowerCase();
                        return item.isAvailable() && (name.contains(text) || description.contains(text));
                    })
                    .collect(Collectors.toList());
        }

    }


}
