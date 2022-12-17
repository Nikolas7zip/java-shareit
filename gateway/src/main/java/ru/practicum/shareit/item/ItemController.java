package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;


@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                          @PathVariable Long itemId) {
        log.info("Get item={}, userId={}", itemId, userId);

        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(
            @RequestHeader(USER_ID_REQUEST_HEADER) long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get items of userId={} with from={}, size={}", userId, from, size);

        return itemClient.getOwnerItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsAvailableToRentByText(
            @RequestHeader(USER_ID_REQUEST_HEADER) long userId,
            @NotNull @RequestParam String text,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get available items by text={}, userId={}, from={}, size={}", text, userId, from, size);

        return itemClient.getItemsAvailableToRentByText(userId, text, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.info("Create item {}, userId={}", itemDto, userId);

        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody ItemDto itemDto) {
        ItemDto partivalValidItemDto = new ItemDto();

        if (itemDto.getName() != null) {
            partivalValidItemDto.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            partivalValidItemDto.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            partivalValidItemDto.setAvailable(itemDto.getAvailable());
        }

        log.info("Update item={} with values {}, userId={}", itemId, itemDto, userId);

        return itemClient.updateItem(userId, itemId, partivalValidItemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createItemComment(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                                    @PathVariable Long itemId,
                                                    @Valid @RequestBody CommentDto commentDto) {
        log.info("Create comment {} for item={}, userId={}", commentDto, itemId, userId);

        return itemClient.createItemComment(userId, itemId, commentDto);
    }

}
