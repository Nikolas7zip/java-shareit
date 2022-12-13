package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.pagination.EntityPagination;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService requestService;

    @Autowired
    public ItemRequestController(ItemRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ItemRequestDto createNewRequest(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                           @Valid @RequestBody ItemRequestDto requestDto) {
        return requestService.create(userId, requestDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequest(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                     @PathVariable Long requestId) {
        return requestService.get(userId, requestId);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId) {
        return requestService.getByRequester(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequestsOtherUsers(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return requestService.getOfOtherUsers(userId, EntityPagination.of(from, size));
    }

}
