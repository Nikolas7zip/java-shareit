package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.dto.BookingShort;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByBooker_Id(Long bookerId, Pageable pageable);

    Page<Booking> findAllByBooker_IdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    Page<Booking> findAllByBooker_IdAndEndBefore(Long bookerId, LocalDateTime timestamp, Pageable pageable);

    Page<Booking> findAllByBooker_IdAndStartAfter(Long bookerId, LocalDateTime timestamp, Pageable pageable);

    @Query(" SELECT b FROM Booking b " +
            "WHERE b.booker.id=?1 AND b.start<=?2 AND b.end >=?2 ")
    Page<Booking> findAllCurrentBookingsByBookerId(Long bookerId, LocalDateTime timestamp, Pageable pageable);

    Page<Booking> findAllByItem_OwnerId(Long ownerId, Pageable pageable);

    Page<Booking> findAllByItem_OwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    Page<Booking> findAllByItem_OwnerIdAndEndBefore(Long ownerId, LocalDateTime timestamp, Pageable pageable);

    Page<Booking> findAllByItem_OwnerIdAndStartAfter(Long ownerId, LocalDateTime timestamp, Pageable pageable);

    @Query(" SELECT b FROM Booking b " +
            "WHERE b.item.ownerId=?1 AND b.start<=?2 AND b.end >=?2 ")
    Page<Booking> findAllCurrentBookingsByOwnerItems(Long ownerId, LocalDateTime timestamp, Pageable pageable);

    @Query(" SELECT b FROM Booking b " +
            "WHERE b.item.id=?1 AND b.status=ru.practicum.shareit.booking.BookingStatus.APPROVED AND " +
            "b.end >= ?2 AND b.start <= ?3")
    List<Booking> findApprovedIntersection(Long itemId, LocalDateTime start, LocalDateTime end);

    @Query(" SELECT new ru.practicum.shareit.booking.dto.BookingShort(b.id, b.booker.id) FROM Booking b " +
            "WHERE b.item.id=?1 AND b.status=ru.practicum.shareit.booking.BookingStatus.APPROVED AND " +
            "(b.end <= ?2 OR (b.start < ?2 AND b.end >= ?2))" +
            "ORDER BY b.end DESC")
    List<BookingShort> findLastBookings(Long itemId, LocalDateTime timestamp);

    @Query(" SELECT new ru.practicum.shareit.booking.dto.BookingShort(b.id, b.booker.id) FROM Booking b " +
            "WHERE b.item.id=?1 AND b.status=ru.practicum.shareit.booking.BookingStatus.APPROVED AND " +
            "b.start > ?2 " +
            "ORDER BY b.start ASC")
    List<BookingShort> findNextBookings(Long itemId, LocalDateTime timestamp);

    @Query(" SELECT b FROM Booking b " +
            "WHERE b.item.id=?1 AND b.booker.id=?2 AND b.status=ru.practicum.shareit.booking.BookingStatus.APPROVED AND " +
            "b.end <= ?3 ")
    List<Booking> findExpiredApprovedBookings(Long itemId, Long bookerId, LocalDateTime timestamp);
}
