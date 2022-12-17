package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";

    private final ItemRequestClient requestClient;

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                             @PathVariable Long requestId) {
        log.info("Get item request={}, userId={}", requestId, userId);

        return requestClient.getRequest(userId, requestId);
    }

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                                @Valid @RequestBody ItemRequestDto requestDto) {
        log.info("Create item request {}, userId={}", requestDto, userId);

        return requestClient.createRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader(USER_ID_REQUEST_HEADER) long userId) {
        log.info("Get item requests for userId={}", userId);

        return requestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequestsOtherUsers(
            @RequestHeader(USER_ID_REQUEST_HEADER) long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get item requests other users with userId={}, from={}, size={}", userId, from, size);

        return requestClient.getRequestsOtherUsers(userId, from, size);
    }
}
