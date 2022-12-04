package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingOutput;
import ru.practicum.shareit.pagination.EntityPagination;

import java.util.List;

@Transactional
@Rollback(false)
@SpringBootTest(properties = "db.test.name=booking",
                webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql("/create_entities.sql")
public class BookingServiceIntegrationTest {
    private final BookingService bookingService;
    private final Long ownerId = 1L;
    private final Long bookerId = 2L;

    @Test
    void shouldFindAllRequesterBookings() {
        List<BookingOutput> bookingOutputs = bookingService.getByBooker(bookerId, QueryBookingState.ALL,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(4, bookingOutputs.size());
    }

    @Test
    void shouldFindPastRequesterBookings() {
        List<BookingOutput> bookingOutputs = bookingService.getByBooker(bookerId, QueryBookingState.PAST,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(1, bookingOutputs.size());
    }

    @Test
    void shouldFindCurrentRequesterBookings() {
        List<BookingOutput> bookingOutputs = bookingService.getByBooker(bookerId, QueryBookingState.CURRENT,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(2, bookingOutputs.size());
    }

    @Test
    void shouldFindFutureRequesterBookings() {
        List<BookingOutput> bookingOutputs = bookingService.getByBooker(bookerId, QueryBookingState.FUTURE,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(1, bookingOutputs.size());
    }

    @Test
    void shouldFindWaitingRequesterBookings() {
        List<BookingOutput> bookingOutputs = bookingService.getByBooker(bookerId, QueryBookingState.WAITING,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(1, bookingOutputs.size());
    }

    @Test
    void shouldFindRejectedRequesterBookings() {
        List<BookingOutput> bookingOutputs = bookingService.getByBooker(bookerId, QueryBookingState.REJECTED,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(1, bookingOutputs.size());
    }

    @Test
    void shouldFindAllBookingsOfOwnerItems() {
        List<BookingOutput> bookingOutputs = bookingService.getByOwnerItems(ownerId, QueryBookingState.ALL,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(4, bookingOutputs.size());
    }

    @Test
    void shouldFindPastBookingsOfOwnerItems() {
        List<BookingOutput> bookingOutputs = bookingService.getByOwnerItems(ownerId, QueryBookingState.PAST,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(1, bookingOutputs.size());
    }

    @Test
    void shouldFindCurrentBookingsOfOwnerItems() {
        List<BookingOutput> bookingOutputs = bookingService.getByOwnerItems(ownerId, QueryBookingState.CURRENT,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(2, bookingOutputs.size());
    }

    @Test
    void shouldFindFutureBookingsOfOwnerItems() {
        List<BookingOutput> bookingOutputs = bookingService.getByOwnerItems(ownerId, QueryBookingState.FUTURE,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(1, bookingOutputs.size());
    }

    @Test
    void shouldFindWaitingBookingsOfOwnerItems() {
        List<BookingOutput> bookingOutputs = bookingService.getByOwnerItems(ownerId, QueryBookingState.WAITING,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(1, bookingOutputs.size());
    }

    @Test
    void shouldFindRejectedBookingsOfOwnerItems() {
        List<BookingOutput> bookingOutputs = bookingService.getByOwnerItems(ownerId, QueryBookingState.REJECTED,
                EntityPagination.of(0, 10));

        Assertions.assertEquals(1, bookingOutputs.size());
    }
}
