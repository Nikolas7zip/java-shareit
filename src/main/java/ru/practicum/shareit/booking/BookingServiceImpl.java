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
import static ru.practicum.shareit.booking.BookingPredicates.*;

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
        validateBooking(booking);

        return BookingMapper.mapToBookingOutput(bookingRepository.save(booking));
    }

    @Transactional
    @Override
    public BookingOutput changeStatus(Long ownerId, Long bookingId, boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new EntityNotFoundException(Booking.class, bookingId));
        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new EntityNotFoundException(Booking.class, bookingId);
        } else if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Can't change booking status");
        }
        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.mapToBookingOutput(bookingRepository.save(booking));
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
        Sort sortDateDesc = Sort.by("start").descending();
        List<Booking> bookings = bookingRepository.findAllByBooker_Id(bookerId, sortDateDesc);

        return filterBookingsByState(bookings, stateFromQuery);
    }

    @Override
    public List<BookingOutput> getByOwnerItems(Long ownerId, String state) {
        QueryBookingState stateFromQuery = parseQueryBookingState(state);
        userRepository.findById(ownerId).orElseThrow(() -> new EntityNotFoundException(User.class, ownerId));
        Sort sortDateDesc = Sort.by("start").descending();
        List<Booking> bookings = bookingRepository.findAllByItem_OwnerId(ownerId, sortDateDesc);

        return filterBookingsByState(bookings, stateFromQuery);
    }

    private QueryBookingState parseQueryBookingState(String state) {
        try {
            return QueryBookingState.valueOf(state);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }

    private List<BookingOutput> filterBookingsByState(List<Booking> bookings, QueryBookingState state) {
        switch (state) {
            case ALL:
                break;
            case WAITING:
                bookings = filterBookings(bookings, isWaiting());
                break;
            case REJECTED:
                bookings = filterBookings(bookings, isRejected());
                break;
            case PAST:
                bookings = filterBookings(bookings, isPast());
                break;
            case FUTURE:
                bookings = filterBookings(bookings, isFuture());
                break;
            case CURRENT:
                bookings = filterBookings(bookings, isCurrent());
                break;
            default:
                bookings = Collections.emptyList();
        }
        return BookingMapper.mapToBookingOutput(bookings);
    }

    private void validateBooking(Booking booking) {
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
