package ru.practicum.shareit.booking;


import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutput;
import ru.practicum.shareit.pagination.EntityPagination;

import java.util.List;

public interface BookingService {
    BookingOutput create(Long bookerId, BookingDto bookingDto);

    BookingOutput changeStatus(Long ownerId, Long bookingId, boolean isApproved);

    BookingOutput get(Long userId, Long bookingId);

    List<BookingOutput> getByBooker(Long bookerId, QueryBookingState state, EntityPagination pagination);

    List<BookingOutput> getByOwnerItems(Long ownerId, QueryBookingState state, EntityPagination pagination);
}
