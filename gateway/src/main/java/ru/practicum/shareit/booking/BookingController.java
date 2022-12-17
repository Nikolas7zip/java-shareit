package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                             @PathVariable Long bookingId) {
        log.info("Get booking={}, userId={}", bookingId, userId);

        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(
            @RequestHeader(USER_ID_REQUEST_HEADER) long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam);
        log.info("Get bookings userId={} with state {}, from={}, size={}", userId, stateParam, from, size);

        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsOfOwnerItems(
            @RequestHeader(USER_ID_REQUEST_HEADER) long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam);
        log.info("Get bookings of items ownerId={} with state={}, from={}, size={}", userId, stateParam, from, size);

        return bookingClient.getBookingsOfOwnerItems(userId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader(USER_ID_REQUEST_HEADER) long userId,
                                           @Valid @RequestBody BookingDto bookingDto) {
        log.info("Create booking {}, userId={}", bookingDto, userId);
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            throw new IllegalArgumentException("Wrong start/end booking datetime");
        }
        return bookingClient.bookItem(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> changeBookingStatus(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                                      @PathVariable Long bookingId,
                                                      @RequestParam(name = "approved") boolean approved) {
        log.info("Change approve status for booking={} to {}, userId={}", bookingId, approved, userId);

        return bookingClient.changeBookingStatus(userId, bookingId, approved);
    }
}
