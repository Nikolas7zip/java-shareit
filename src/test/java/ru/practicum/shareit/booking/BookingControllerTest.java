package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutput;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private final BookingDto bookingDto = new BookingDto(1L, 1L,
            LocalDateTime.now().withNano(0),
            LocalDateTime.now().plusDays(2).withNano(0));
    private BookingOutput bookingOutput;
    private final String ownerId = "1";
    private final String bookerId = "3";
    private final UserDto userDto = new UserDto(3L, "Booker", "booker@mail.com");
    private final ItemDto itemDto = new ItemDto(2L, "Инструмент", "Супер инструмент", true);

    @BeforeEach
    void setUp() {
        bookingOutput = new BookingOutput();
        bookingOutput.setId(bookingDto.getId());
        bookingOutput.setStart(bookingDto.getStart());
        bookingOutput.setEnd(bookingDto.getEnd());
        bookingOutput.setStatus(BookingStatus.WAITING);
        bookingOutput.setBooker(userDto);
        bookingOutput.setItem(itemDto);
    }

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingService.create(anyLong(), any()))
                .thenReturn(bookingOutput);

        mockMvc.perform(post("/bookings")
                            .header("X-Sharer-User-Id", bookerId)
                            .content(mapper.writeValueAsString(bookingDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutput.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingOutput.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingOutput.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingOutput.getStatus().name())))
                .andExpect(jsonPath("$.booker.id", is(bookingOutput.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutput.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingOutput.getItem().getName())));
    }

    @Test
    void shouldReturnBadRequestWhenCreateFailBooking() throws Exception {
        BookingDto failBookingDto = new BookingDto(1L, null, null, null);
        when(bookingService.create(anyLong(), any()))
                .thenReturn(null);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .content(mapper.writeValueAsString(failBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBooking() throws Exception {
        when(bookingService.get(anyLong(), anyLong()))
                .thenReturn(bookingOutput);

        mockMvc.perform(get("/bookings/{bookingId}", bookingOutput.getId())
                        .header("X-Sharer-User-Id", bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutput.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingOutput.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingOutput.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingOutput.getStatus().name())))
                .andExpect(jsonPath("$.booker.id", is(bookingOutput.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutput.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingOutput.getItem().getName())));
    }

    @Test
    void shouldApproveBooking() throws Exception {
        bookingOutput.setStatus(BookingStatus.APPROVED);
        when(bookingService.changeStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingOutput);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingOutput.getId())
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutput.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingOutput.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingOutput.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingOutput.getStatus().name())))
                .andExpect(jsonPath("$.booker.id", is(bookingOutput.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutput.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingOutput.getItem().getName())));
    }

    @Test
    void shouldReturnBookerBookings() throws Exception {
        when(bookingService.getByBooker(anyLong(), any(), any()))
                .thenReturn(List.of(bookingOutput));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingOutput.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingOutput.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingOutput.getEnd().toString())))
                .andExpect(jsonPath("$[0].status", is(bookingOutput.getStatus().name())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingOutput.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingOutput.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingOutput.getItem().getName())));
    }

    @Test
    void shouldReturnBadRequestWhenGetBookingsWithUnknownState() throws Exception {
        when(bookingService.getByBooker(anyLong(), any(), any()))
                .thenReturn(null);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .param("state", "LOL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBookingsOfOwnerItems() throws Exception {
        when(bookingService.getByOwnerItems(anyLong(), any(), any()))
                .thenReturn(List.of(bookingOutput));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingOutput.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingOutput.getStart().toString())))
                .andExpect(jsonPath("$[0].end", is(bookingOutput.getEnd().toString())))
                .andExpect(jsonPath("$[0].status", is(bookingOutput.getStatus().name())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingOutput.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingOutput.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookingOutput.getItem().getName())));
    }

}
