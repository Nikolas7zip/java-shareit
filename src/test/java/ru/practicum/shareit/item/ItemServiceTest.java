package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentOutput;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.EntityPagination;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository mockItemRepository;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private BookingRepository mockBookingRepository;

    @Mock
    private CommentRepository mockCommentRepository;

    @Mock
    private ItemRequestRepository mockRequestRepository;

    private ItemService itemService;
    private ItemDto itemDto = new ItemDto(1L, "Лопата", "Лопата для огорода", true);
    private User user;
    private Item item;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(
                mockItemRepository,
                mockUserRepository,
                mockBookingRepository,
                mockCommentRepository,
                mockRequestRepository
        );

        user = new User();
        user.setId(1L);
        user.setName("Tester");
        user.setEmail("test@mail.com");

        item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwnerId(user.getId());
    }

    @Test
    void shouldCreateItem() {
        when(mockUserRepository.existsById(anyLong())).thenReturn(true);
        when(mockItemRepository.save(any())).thenReturn(item);

        ItemDto createdItem = itemService.create(user.getId(), itemDto);

        assertEquals(itemDto, createdItem);
    }

    @Test
    void shouldCreateItemOnRequest() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        ItemDto itemDtoOnRequest = new ItemDto(1L, "Лопата", "Лопата для огорода", true);
        itemDtoOnRequest.setRequestId(request.getId());
        item.setRequestId(request.getId());

        when(mockUserRepository.existsById(anyLong())).thenReturn(true);
        when(mockItemRepository.save(any())).thenReturn(item);
        when(mockRequestRepository.findById(anyLong())).thenReturn(Optional.of(request));

        ItemDto createdItem = itemService.create(user.getId(), itemDtoOnRequest);

        assertEquals(itemDtoOnRequest, createdItem);
    }

    @Test
    void shouldFindItem() {
        when(mockUserRepository.existsById(anyLong())).thenReturn(true);
        when(mockItemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(mockBookingRepository.findNextBookings(anyLong(), any())).thenReturn(new ArrayList<>());
        when(mockBookingRepository.findLastBookings(anyLong(), any())).thenReturn(new ArrayList<>());
        when(mockCommentRepository.findItemComments(anyLong())).thenReturn(null);

        ItemDto findItem = itemService.get(user.getId(), itemDto.getId());

        assertEquals(itemDto, findItem);
    }

    @Test
    void shouldFindAllOwnerItems() {
        List<Item> itemList = List.of(item);
        Page<Item> itemPage = new PageImpl<>(itemList);
        when(mockUserRepository.existsById(anyLong())).thenReturn(true);
        when(mockItemRepository.findAllByOwnerId(anyLong(), any())).thenReturn(itemPage);
        when(mockBookingRepository.findNextBookings(anyLong(), any())).thenReturn(new ArrayList<>());
        when(mockBookingRepository.findLastBookings(anyLong(), any())).thenReturn(new ArrayList<>());
        when(mockCommentRepository.findItemComments(anyLong())).thenReturn(null);

        List<ItemDto> findItems = itemService.getByOwner(user.getId(), new EntityPagination(0, 10));

        assertEquals(List.of(itemDto), findItems);
    }

    @Test
    void shouldFindAvailableToRentItems() {
        List<Item> itemList = List.of(item);
        Page<Item> itemPage = new PageImpl<>(itemList);
        when(mockUserRepository.existsById(anyLong())).thenReturn(true);
        when(mockItemRepository.findAvailableToRentByText(anyString(), any())).thenReturn(itemPage);

        List<ItemDto> findItems = itemService.getAvailableToRentByText(user.getId(), "Лопата",
                new EntityPagination(0, 10));

        assertEquals(List.of(itemDto), findItems);
    }

    @Test
    void shouldReturnEmptyListWhenTryFindItemsWithBlankText() {
        when(mockUserRepository.existsById(anyLong())).thenReturn(true);

        List<ItemDto> findItems = itemService.getAvailableToRentByText(user.getId(), "  ",
                new EntityPagination(0, 10));

        assertTrue(findItems.isEmpty());
    }

    @Test
    void shouldThrowWhenNotFoundItem() {
        when(mockUserRepository.existsById(anyLong())).thenReturn(true);
        when(mockItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemService.get(user.getId(), 100L));
    }

    @Test
    void shouldUpdateItem() {
        ItemDto requestItemDto = new ItemDto(1L, "Лопата", "Универсальная лопата", false);
        item.setDescription(requestItemDto.getDescription());
        item.setAvailable(requestItemDto.getAvailable());

        when(mockItemRepository.findByIdAndOwnerId(anyLong(), anyLong())).thenReturn(Optional.of(item));
        when(mockItemRepository.save(any())).thenReturn(item);

        ItemDto updatedItem = itemService.update(user.getId(), requestItemDto);
        assertEquals(requestItemDto, updatedItem);
    }

    @Test
    void shouldThrowWhenNonOwnerTryUpdateItem() {
        when(mockItemRepository.findByIdAndOwnerId(anyLong(), anyLong())).thenReturn(Optional.empty());
        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemService.update(2L, itemDto));
    }

    @Test
    void shouldCreateComment() {
        CommentDto commentDto = new CommentDto(1L, "Отличная вещь, рекомендую");
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.APPROVED);
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setItemId(item.getId());
        comment.setText(commentDto.getText());
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now().withNano(0));

        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(mockBookingRepository.findExpiredApprovedBookings(anyLong(), anyLong(), any()))
                .thenReturn(List.of(booking));
        when(mockCommentRepository.save(any())).thenReturn(comment);

        CommentOutput expectedComment = new CommentOutput(1L, commentDto.getText(), user.getName(), comment.getCreated());
        CommentOutput createdComment = itemService.createComment(user.getId(), item.getId(), commentDto);
        assertEquals(expectedComment, createdComment);
    }

    @Test
    void shouldThrowWhenCreateCommentWithoutBooking() {
        CommentDto commentDto = new CommentDto(1L, "Отличная вещь, рекомендую");
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(mockBookingRepository.findExpiredApprovedBookings(anyLong(), anyLong(), any()))
                .thenReturn(new ArrayList<>());
        final BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> itemService.createComment(user.getId(), item.getId(), commentDto)
        );
    }
}
