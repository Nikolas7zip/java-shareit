package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutput;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.EntityPagination;
import ru.practicum.shareit.user.UserMapper;
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
public class BookingServiceTest {

    @Mock
    private BookingRepository mockBookingRepository;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private ItemRepository mockItemRepository;

    private BookingService bookingService;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingOutput expectedBooking;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                mockBookingRepository,
                mockUserRepository,
                mockItemRepository);
        booker = new User();
        booker.setId(2L);
        booker.setName("Admin");
        booker.setEmail("admin@mail.com");

        item = new Item();
        item.setId(1L);
        item.setName("Лопата");
        item.setDescription("Лопата для огорода");
        item.setAvailable(true);
        item.setOwnerId(1L);

        booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().withNano(0));
        booking.setEnd(LocalDateTime.now().withNano(0).plusDays(2));
        booking.setBooker(booker);
        booking.setItem(item);

        expectedBooking = new BookingOutput(booking.getId(), booking.getStart(), booking.getEnd(), booking.getStatus(),
                ItemMapper.mapToItemDto(item), UserMapper.mapToUserDto(booker));
    }

    @Test
    void shouldCreateBooking() {
        BookingDto bookingDto = new BookingDto(1L, item.getId(), booking.getStart(), booking.getEnd());
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(mockItemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(mockBookingRepository.findApprovedIntersection(anyLong(), any(), any())).thenReturn(new ArrayList<>());
        when(mockBookingRepository.save(any())).thenReturn(booking);

        BookingOutput bookingOutput = bookingService.create(booker.getId(), bookingDto);
        assertEquals(expectedBooking, bookingOutput);
    }

    @Test
    void shouldThrowWhenItemNotFound() {
        BookingDto bookingDto = new BookingDto(1L, 100L, booking.getStart(), booking.getEnd());
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(mockItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.create(booker.getId(), bookingDto)
        );
    }

    @Test
    void shouldThrowWhenItemNotAvailable() {
        item.setAvailable(false);
        BookingDto bookingDto = new BookingDto(1L, item.getId(), booking.getStart(), booking.getEnd());
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(mockItemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        final BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookingService.create(booker.getId(), bookingDto)
        );
    }

    @Test
    void shouldThrowWhenWrongBookingDate() {
        BookingDto bookingDto = new BookingDto(1L, item.getId(), booking.getStart(), LocalDateTime.now().minusDays(1));
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(mockItemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        final BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookingService.create(booker.getId(), bookingDto)
        );
    }

    @Test
    void shouldThrowWhenBookingIntersection() {
        Booking anotherBooking = new Booking();
        anotherBooking.setId(15L);
        anotherBooking.setStatus(BookingStatus.APPROVED);

        BookingDto bookingDto = new BookingDto(1L, item.getId(), booking.getStart(), booking.getEnd());
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(mockItemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(mockBookingRepository.findApprovedIntersection(anyLong(), any(), any())).thenReturn(List.of(anotherBooking));

        final BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookingService.create(booker.getId(), bookingDto)
        );
    }

    @Test
    void shouldThrowWhenBookerIsOwnerItem() {
        BookingDto bookingDto = new BookingDto(1L, item.getId(), booking.getStart(), booking.getEnd());
        item.setOwnerId(booker.getId());
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(mockItemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.create(booker.getId(), bookingDto)
        );
    }

    @Test
    void shouldChangeBookingStatus() {
        Booking bookingChangedStatus = new Booking();
        bookingChangedStatus.setId(1L);
        bookingChangedStatus.setStatus(BookingStatus.APPROVED);
        bookingChangedStatus.setStart(booking.getStart());
        bookingChangedStatus.setEnd(booking.getEnd());
        bookingChangedStatus.setBooker(booker);
        bookingChangedStatus.setItem(item);

        expectedBooking.setStatus(BookingStatus.APPROVED);
        when(mockBookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(mockBookingRepository.save(any())).thenReturn(bookingChangedStatus);

        BookingOutput bookingOutput = bookingService.changeStatus(item.getOwnerId(), booking.getId(), true);
        assertEquals(expectedBooking, bookingOutput);
    }

    @Test
    void shouldThrowWhenChangeStatusNotOwner() {
        when(mockBookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.changeStatus(booker.getId(), booking.getId(), true)
        );
    }

    @Test
    void shouldThrowWhenChangeStatusForNotWaitingBooking() {
        booking.setStatus(BookingStatus.REJECTED);
        when(mockBookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        final BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookingService.changeStatus(item.getOwnerId(), booking.getId(), true)
        );
    }

    @Test
    void shouldFindBooking() {
        when(mockBookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingOutput bookingOutput = bookingService.get(booker.getId(),booking.getId());
        assertEquals(expectedBooking, bookingOutput);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getByBooker(99L, QueryBookingState.ALL, null)
        );
    }

    @Test
    void shouldThrowWhenFindBookingUnknownUser() {
        when(mockBookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.get(100L,booking.getId())
        );
    }

    @Test
    void shouldFindBookerBookings() {
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(mockBookingRepository.findAllByBooker_Id(anyLong(), any())).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingOutput> bookingOutputs = bookingService.getByBooker(booker.getId(), QueryBookingState.ALL,
                EntityPagination.of(0, 10));

        assertEquals(List.of(expectedBooking), bookingOutputs);
    }

    @Test
    void shouldFindOwnerBookings() {
        User owner = new User();
        owner.setId(1L);
        owner.setName("Tester");
        owner.setEmail("test@mail.com");
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(mockBookingRepository.findAllByItem_OwnerId(anyLong(), any())).thenReturn(new PageImpl<>(List.of(booking)));

        List<BookingOutput> bookingOutputs = bookingService.getByOwnerItems(owner.getId(), QueryBookingState.ALL,
                EntityPagination.of(0, 10));

        assertEquals(List.of(expectedBooking), bookingOutputs);
    }

}
