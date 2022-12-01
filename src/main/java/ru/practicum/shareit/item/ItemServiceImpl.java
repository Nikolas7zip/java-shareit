package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShort;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.comment.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.EntityPagination;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository,
                           ItemRequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public ItemDto get(Long userId, Long id) {
        throwIfUserNotFound(userId);
        Item item = itemRepository.findById(id)
                                  .orElseThrow(() -> new EntityNotFoundException(Item.class, id));

        ItemDto dto = ItemMapper.mapToItemDto(item);
        if (item.getOwnerId().equals(userId)) {
            dto.setLastBooking(findLastItemBooking(dto.getId()));
            dto.setNextBooking(findNextItemBooking(dto.getId()));
        }
        dto.setComments(commentRepository.findItemComments(id));

        return dto;
    }

    @Override
    public List<ItemDto> getByOwner(Long userId, EntityPagination pagination) {
        throwIfUserNotFound(userId);
        Pageable sortPage = PageRequest.of(pagination.getPage(), pagination.getSize(), Sort.by("id").ascending());
        Page<Item> page = itemRepository.findAllByOwnerId(userId, sortPage);
        List<ItemDto> dtos = ItemMapper.mapToItemDto(page.getContent());
        for (ItemDto dto : dtos) {
            dto.setLastBooking(findLastItemBooking(dto.getId()));
            dto.setNextBooking(findNextItemBooking(dto.getId()));
            dto.setComments(commentRepository.findItemComments(dto.getId()));
        }

        return dtos;
    }

    @Override
    public List<ItemDto> getAvailableToRentByText(Long userId, String text, EntityPagination pagination) {
        throwIfUserNotFound(userId);

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(pagination.getPage(), pagination.getSize());
        Page<Item> page = itemRepository.findAvailableToRentByText(text, pageable);
        return ItemMapper.mapToItemDto(page.getContent());
    }

    @Transactional
    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        throwIfUserNotFound(userId);

        Long requestId = itemDto.getRequestId();
        if (requestId != null) {
            requestRepository.findById(requestId)
                             .orElseThrow(() -> new EntityNotFoundException(ItemRequest.class, requestId));
        }

        Item item = itemRepository.save(ItemMapper.mapToItem(itemDto, userId));
        log.info("Created " + item);

        return ItemMapper.mapToItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, ItemDto itemDto) {
        Item item = itemRepository.findByIdAndOwnerId(itemDto.getId(), userId)
                .orElseThrow(() -> new EntityNotFoundException(Item.class, itemDto.getId()));

        ItemDto databaseItemDto = ItemMapper.mapToItemDto(item);
        if (itemDto.getName() != null) {
            databaseItemDto.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            databaseItemDto.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            databaseItemDto.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(ItemMapper.mapToItem(databaseItemDto, userId));
        log.info("Updated " + updatedItem);

        return ItemMapper.mapToItemDto(updatedItem);
    }

    @Transactional
    @Override
    public CommentOutput createComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
        if (bookingRepository.findExpiredApprovedBookings(itemId, userId, LocalDateTime.now()).isEmpty()) {
            throw new BadRequestException("Failed booking");
        }
        Comment comment = commentRepository.save(CommentMapper.mapToNewComment(commentDto, itemId, user));
        log.info("Created " + comment);

        return CommentMapper.mapToCommentDto(comment);
    }

    private void throwIfUserNotFound(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(User.class, userId);
        }
    }

    private BookingShort findLastItemBooking(Long itemId) {
        List<BookingShort> bookings = bookingRepository.findLastBookings(itemId, LocalDateTime.now());
        if (bookings.isEmpty()) {
            return null;
        } else {
            return bookings.get(0);
        }
    }

    private BookingShort findNextItemBooking(Long itemId) {
        List<BookingShort> bookings = bookingRepository.findNextBookings(itemId, LocalDateTime.now());
        if (bookings.isEmpty()) {
            return null;
        } else {
            return bookings.get(0);
        }
    }
}
