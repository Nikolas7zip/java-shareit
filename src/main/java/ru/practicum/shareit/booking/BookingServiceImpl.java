package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutput;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static ru.practicum.shareit.booking.BookingStatus.*;


@Slf4j
@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    @Override
    public BookingOutput create(Long bookerId, BookingDto bookingDto) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, bookerId));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException(Item.class, bookingDto.getItemId()));
        Booking booking = BookingMapper.mapToNewBooking(bookingDto, booker, item);
        throwIfBookingIsNotValid(booking);
        Booking bookingDb = bookingRepository.save(booking);
        log.info("Created " + bookingDb);

        return BookingMapper.mapToBookingOutput(bookingDb);
    }

    @Transactional
    @Override
    public BookingOutput changeStatus(Long ownerId, Long bookingId, boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(Booking.class, bookingId));
        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new EntityNotFoundException(Booking.class, bookingId);
        } else if (booking.getStatus() != WAITING) {
            throw new BadRequestException("Can't change booking status");
        }
        booking.setStatus(isApproved ? APPROVED : REJECTED);
        Booking bookingUpdated = bookingRepository.save(booking);
        log.info("Updated with status " + bookingUpdated);

        return BookingMapper.mapToBookingOutput(bookingUpdated);
    }

    @Override
    public BookingOutput get(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(Booking.class, bookingId));
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwnerId().equals(userId)) {
            return BookingMapper.mapToBookingOutput(booking);
        }
        throw new EntityNotFoundException(Booking.class, bookingId);
    }

    @Override
    public List<BookingOutput> getByBooker(Long bookerId, String state) {
        QueryBookingState stateFromQuery = parseQueryBookingState(state);
        userRepository.findById(bookerId).orElseThrow(() -> new EntityNotFoundException(User.class, bookerId));

        return findBookingsByBooker(bookerId, stateFromQuery);
    }

    @Override
    public List<BookingOutput> getByOwnerItems(Long ownerId, String state) {
        QueryBookingState stateFromQuery = parseQueryBookingState(state);
        userRepository.findById(ownerId).orElseThrow(() -> new EntityNotFoundException(User.class, ownerId));

        return findBookingsOfOwnerItems(ownerId, stateFromQuery);
    }

    private QueryBookingState parseQueryBookingState(String state) {
        try {
            return QueryBookingState.valueOf(state);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }

    private List<BookingOutput> findBookingsByBooker(Long bookerId, QueryBookingState state) {
        Sort sortDateDesc = Sort.by("start").descending();
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBooker_Id(bookerId, sortDateDesc);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBooker_IdAndStatus(bookerId, WAITING, sortDateDesc);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBooker_IdAndStatus(bookerId, REJECTED, sortDateDesc);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBooker_IdAndEndBefore(bookerId, LocalDateTime.now(), sortDateDesc);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBooker_IdAndStartAfter(bookerId, LocalDateTime.now(), sortDateDesc);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllCurrentBookingsByBookerId(bookerId, LocalDateTime.now());
                break;
            default:
                bookings = Collections.emptyList();
        }
        return BookingMapper.mapToBookingOutput(bookings);
    }

    private List<BookingOutput> findBookingsOfOwnerItems(Long ownerId, QueryBookingState state) {
        Sort sortDateDesc = Sort.by("start").descending();
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByItem_OwnerId(ownerId, sortDateDesc);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItem_OwnerIdAndStatus(ownerId, WAITING, sortDateDesc);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItem_OwnerIdAndStatus(ownerId, REJECTED, sortDateDesc);
                break;
            case PAST:
                bookings = bookingRepository.findAllByItem_OwnerIdAndEndBefore(ownerId, LocalDateTime.now(), sortDateDesc);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItem_OwnerIdAndStartAfter(ownerId, LocalDateTime.now(), sortDateDesc);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllCurrentBookingsByOwnerItems(ownerId, LocalDateTime.now());
                break;
            default:
                bookings = Collections.emptyList();
        }
        return BookingMapper.mapToBookingOutput(bookings);
    }

    private void throwIfBookingIsNotValid(Booking booking) {
        LocalDateTime allowedStartOfBooking = LocalDateTime.now().minusMinutes(1);
        LocalDateTime start = booking.getStart();
        LocalDateTime end = booking.getEnd();
        boolean isStartAfterEnd = start.isAfter(end);
        boolean isStartInPast = start.isBefore(allowedStartOfBooking);
        Item item = booking.getItem();
        if (isStartAfterEnd || isStartInPast) {
            throw new BadRequestException("Wrong start/end booking datetime");
        } else if (!item.isAvailable()) {
            throw new BadRequestException("Item not available");
        } else if (booking.getBooker().getId().equals(item.getOwnerId())) {
            throw new EntityNotFoundException(Item.class, item.getId());
        } else if (!bookingRepository.findApprovedIntersection(item.getId(), start, end).isEmpty()) {
            throw new BadRequestException("Intersection with approved bookings");
        }
    }
}
