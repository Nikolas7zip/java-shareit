package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutput;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.pagination.EntityPagination;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String USER_ID_REQUEST_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingOutput createNewBooking(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                          @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutput changeBookingStatus(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                             @PathVariable Long bookingId,
                                             @RequestParam boolean approved) {
        return bookingService.changeStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingOutput getBooking(@RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
                                    @PathVariable Long bookingId) {
        return bookingService.get(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutput> getUserBookings(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int size) {
        QueryBookingState stateFromQuery = convert(state);
        return bookingService.getByBooker(userId, stateFromQuery, EntityPagination.of(from, size));
    }

    @GetMapping("/owner")
    public List<BookingOutput> getBookingsOfOwnerItems(
            @RequestHeader(USER_ID_REQUEST_HEADER) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int size) {
        QueryBookingState stateFromQuery = convert(state);
        return bookingService.getByOwnerItems(userId, stateFromQuery, EntityPagination.of(from, size));
    }

    private QueryBookingState convert(String state) {
        try {
            return QueryBookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}
