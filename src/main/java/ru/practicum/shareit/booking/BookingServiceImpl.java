package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutput;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.EntityPagination;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
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
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwnerId().equals(userId)) {
            throw new EntityNotFoundException(Booking.class, bookingId);
        }
        return BookingMapper.mapToBookingOutput(booking);
    }

    @Override
    public List<BookingOutput> getByBooker(Long bookerId, QueryBookingState state, EntityPagination pagination) {
        userRepository.findById(bookerId).orElseThrow(() -> new EntityNotFoundException(User.class, bookerId));
        Pageable sortPage = PageRequest.of(pagination.getPage(), pagination.getSize(), Sort.by("start").descending());
        return findBookingsByBooker(bookerId, state, sortPage);
    }

    @Override
    public List<BookingOutput> getByOwnerItems(Long ownerId, QueryBookingState state, EntityPagination pagination) {
        userRepository.findById(ownerId).orElseThrow(() -> new EntityNotFoundException(User.class, ownerId));
        Pageable sortPage = PageRequest.of(pagination.getPage(), pagination.getSize(), Sort.by("start").descending());
        return findBookingsOfOwnerItems(ownerId, state, sortPage);
    }

    private List<BookingOutput> findBookingsByBooker(Long bookerId, QueryBookingState state, Pageable pageable) {
        Page<Booking> page = Page.empty();
        switch (state) {
            case ALL:
                page = bookingRepository.findAllByBooker_Id(bookerId, pageable);
                break;
            case WAITING:
                page = bookingRepository.findAllByBooker_IdAndStatus(bookerId, WAITING, pageable);
                break;
            case REJECTED:
                page = bookingRepository.findAllByBooker_IdAndStatus(bookerId, REJECTED, pageable);
                break;
            case PAST:
                page = bookingRepository.findAllByBooker_IdAndEndBefore(bookerId, LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                page = bookingRepository.findAllByBooker_IdAndStartAfter(bookerId, LocalDateTime.now(), pageable);
                break;
            case CURRENT:
                page = bookingRepository.findAllCurrentBookingsByBookerId(bookerId, LocalDateTime.now(), pageable);
                break;
        }
        return BookingMapper.mapToBookingOutput(page.getContent());
    }

    private List<BookingOutput> findBookingsOfOwnerItems(Long ownerId, QueryBookingState state, Pageable pageable) {
        Page<Booking> page = Page.empty();
        switch (state) {
            case ALL:
                page = bookingRepository.findAllByItem_OwnerId(ownerId, pageable);
                break;
            case WAITING:
                page = bookingRepository.findAllByItem_OwnerIdAndStatus(ownerId, WAITING, pageable);
                break;
            case REJECTED:
                page = bookingRepository.findAllByItem_OwnerIdAndStatus(ownerId, REJECTED, pageable);
                break;
            case PAST:
                page = bookingRepository.findAllByItem_OwnerIdAndEndBefore(ownerId, LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                page = bookingRepository.findAllByItem_OwnerIdAndStartAfter(ownerId, LocalDateTime.now(), pageable);
                break;
            case CURRENT:
                page = bookingRepository.findAllCurrentBookingsByOwnerItems(ownerId, LocalDateTime.now(), pageable);
                break;
        }
        return BookingMapper.mapToBookingOutput(page.getContent());
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
