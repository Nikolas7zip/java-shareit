package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
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
        return itemService.getItem(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId) {
        return itemService.getOwnerItems(userId);
    }

    @PostMapping
    public ItemDto createNewItem(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                 @Valid @RequestBody ItemDto itemDto) {
        return itemService.createNewItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);

        return itemService.updateItem(userId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemsToRentByText(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                               @RequestParam String text) {
        return itemService.findItemsToRentByText(userId, text.toLowerCase());
    }

}
