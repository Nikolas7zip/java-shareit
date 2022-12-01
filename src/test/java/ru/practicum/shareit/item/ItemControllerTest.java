package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingShort;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentOutput;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private final String ownerId = "1";
    private final String bookerId = "2";
    private final ItemDto itemDto = new ItemDto(1L, "Лопата", "Лопата для огорода", true);

    @Test
    void shouldCreateItem() throws Exception {
        when(itemService.create(anyLong(), any()))
                .thenReturn(itemDto);

        mockMvc.perform(post("/items")
                            .header("X-Sharer-User-Id", ownerId)
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void shouldReturnBadRequestWhenCreateItemWithBlankName() throws Exception {
        ItemDto blankNameItemDto = new ItemDto(1L, " ", "Unknown", false);
        when(itemService.create(anyLong(), any()))
                .thenReturn(null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .content(mapper.writeValueAsString(blankNameItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreateItemWithoutAvailable() throws Exception {
        ItemDto itemDtoWithoutAvailable = new ItemDto(1L, "Item", "Item description", null);
        when(itemService.create(anyLong(), any()))
                .thenReturn(null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .content(mapper.writeValueAsString(itemDtoWithoutAvailable))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindItem() throws Exception {
        when(itemService.get(anyLong(), anyLong()))
                .thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", itemDto.getId())
                        .header("X-Sharer-User-Id", ownerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }

    @Test
    void shouldFindItemWithCommentsAndBookings() throws Exception {
        ItemDto itemDtoFull =  new ItemDto(1L, "Лопата", "Лопата для огорода", true);
        CommentOutput commentOutput = new CommentOutput(1L, "Хорошая лопата", "Admin", LocalDateTime.now().withNano(0));
        BookingShort lastBooking = new BookingShort(1L, 2L);
        itemDtoFull.setComments(List.of(commentOutput));
        itemDtoFull.setLastBooking(lastBooking);

        when(itemService.get(anyLong(), anyLong()))
                .thenReturn(itemDtoFull);

        mockMvc.perform(get("/items/{itemId}", itemDto.getId())
                        .header("X-Sharer-User-Id", ownerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoFull.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoFull.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoFull.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoFull.getAvailable())))
                .andExpect(jsonPath("$.lastBooking", is(notNullValue())))
                .andExpect(jsonPath("$.nextBooking", is(nullValue())))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].id", is(commentOutput.getId()), Long.class))
                .andExpect(jsonPath("$.comments[0].text", is(commentOutput.getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(commentOutput.getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created", is(commentOutput.getCreated().toString())));
    }

    @Test
    void shouldReturnInternalErrorWithoutUserHeader() throws Exception {
        when(itemService.get(anyLong(), anyLong()))
                .thenReturn(null);
        mockMvc.perform(get("/items/{itemId}", itemDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemDto updatedItemDto = new ItemDto(1L, "Лопата", "Универсальная лопата", false);
        String json = "{ \"description\" : \"Универсальная лопата\", \"available\" : false }";
        when(itemService.update(anyLong(), any()))
                .thenReturn(updatedItemDto);

        mockMvc.perform(patch("/items/{itemId}", updatedItemDto.getId())
                            .header("X-Sharer-User-Id", ownerId)
                            .content(json)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedItemDto.getName())))
                .andExpect(jsonPath("$.description", is(updatedItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(updatedItemDto.getAvailable())));
    }

    @Test
    void shouldFindAvailableItemsByText() throws Exception {
        when(itemService.getAvailableToRentByText(anyLong(), anyString(), any()))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                            .header("X-Sharer-User-Id", ownerId)
                            .param("text", "лопата")
                            .characterEncoding(StandardCharsets.UTF_8)
                            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())));
    }

    @Test
    void shouldFindOwnerItems() throws Exception {
        ItemDto secondItemDto = new ItemDto(2L, "Гиря", "Гиря, чтобы качать мышцы", true);
        when(itemService.getByOwner(anyLong(), any()))
                .thenReturn(List.of(itemDto, secondItemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[1].id", is(secondItemDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(secondItemDto.getName())))
                .andExpect(jsonPath("$[1].description", is(secondItemDto.getDescription())))
                .andExpect(jsonPath("$[1].available", is(secondItemDto.getAvailable())));
    }

    @Test
    void shouldCreateComment() throws Exception {
        LocalDateTime created = LocalDateTime.now().withNano(0);
        String commentText = "Хорошая лопата";
        CommentDto commentDto = new CommentDto(1L, commentText);
        CommentOutput commentOut = new CommentOutput(1L, commentText, "Admin", created);
        when(itemService.createComment(anyLong(), anyLong(), any()))
                .thenReturn(commentOut);

        mockMvc.perform(post("/items/{itemId}/comment", itemDto.getId())
                            .header("X-Sharer-User-Id", bookerId)
                            .content(mapper.writeValueAsString(commentDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentOut.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentOut.getText())))
                .andExpect(jsonPath("$.authorName", is(commentOut.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentOut.getCreated().toString())));
    }

}
