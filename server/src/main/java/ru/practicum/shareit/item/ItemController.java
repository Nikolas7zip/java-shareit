package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentOutput;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.pagination.EntityPagination;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                           @PathVariable Long itemId) {
        return itemService.get(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                       @RequestParam int from,
                                       @RequestParam int size) {
        return itemService.getByOwner(userId, EntityPagination.of(from, size));
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsAvailableToRentByText(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                                       @RequestParam String text,
                                                       @RequestParam int from,
                                                       @RequestParam int size) {
        return itemService.getAvailableToRentByText(userId, text.toLowerCase(), EntityPagination.of(from, size));
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                              @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);

        return itemService.update(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentOutput createComment(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                       @PathVariable Long itemId,
                                       @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }
}
