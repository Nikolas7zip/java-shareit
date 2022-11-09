package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemDto get(Long userId, Long id) {
        throwIfUserNotFound(userId);
        Item item = itemRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Item id=" + id + " not found")
        );

        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public List<ItemDto> getByOwner(Long userId) {
        throwIfUserNotFound(userId);

        return ItemMapper.mapToItemDto(itemRepository.findByOwner(userId));
    }

    @Override
    public List<ItemDto> getAvailableToRentByText(Long userId, String text) {
        throwIfUserNotFound(userId);

        return ItemMapper.mapToItemDto(itemRepository.findAvailableToRentByText(text));
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        throwIfUserNotFound(userId);
        Item item = itemRepository.add(ItemMapper.mapToItem(itemDto, userId));
        log.info("Created " + item);

        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto update(Long userId, ItemDto itemDto) {
        throwIfUserIsNotItemOwner(userId, itemDto.getId());
        ItemDto databaseItemDto = get(userId, itemDto.getId());
        if (itemDto.getName() != null) {
            databaseItemDto.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            databaseItemDto.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            databaseItemDto.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.update(ItemMapper.mapToItem(databaseItemDto, userId));
        log.info("Updated " + updatedItem);

        return ItemMapper.mapToItemDto(updatedItem);
    }

    private void throwIfUserNotFound(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("User id=" + userId + " not found")
        );
    }

    private void throwIfUserIsNotItemOwner(Long userId, Long itemId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty() || !itemOptional.get().getOwnerId().equals(userId)) {
            throw new EntityNotFoundException("Item id=" + itemId + " not found");
        }
    }
}
