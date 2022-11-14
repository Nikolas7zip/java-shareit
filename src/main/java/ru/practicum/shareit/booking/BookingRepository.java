package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.dto.BookingShort;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker_Id(Long bookerId, Sort sort);

    List<Booking> findAllByItem_OwnerId(Long ownerId, Sort sort);

    @Query(" select b from Booking b " +
            "where b.item.id=?1 and b.status=ru.practicum.shareit.booking.BookingStatus.APPROVED and " +
            "b.end >= ?2 and b.start <= ?3")
    List<Booking> findApprovedIntersection(Long itemId, LocalDateTime start, LocalDateTime end);

    @Query(" select new ru.practicum.shareit.booking.dto.BookingShort(b.id, b.booker.id) from Booking b " +
            "where b.item.id=?1 and b.status=ru.practicum.shareit.booking.BookingStatus.APPROVED and " +
            "(b.end <= ?2 or (b.start < ?2 and b.end >= ?2))" +
            "order by b.end desc")
    List<BookingShort> findLastBookings(Long itemId, LocalDateTime timestamp);

    @Query(" select new ru.practicum.shareit.booking.dto.BookingShort(b.id, b.booker.id) from Booking b " +
            "where b.item.id=?1 and b.status=ru.practicum.shareit.booking.BookingStatus.APPROVED and " +
            "b.start > ?2 " +
            "order by b.start asc")
    List<BookingShort> findNextBookings(Long itemId, LocalDateTime timestamp);

    @Query(" select b from Booking b " +
            "where b.item.id=?1 and b.booker.id=?2 and b.status=ru.practicum.shareit.booking.BookingStatus.APPROVED and " +
            "b.end <= ?3 ")
    List<Booking> findExpiredApprovedBookings(Long itemId, Long bookerId, LocalDateTime timestamp);
}
