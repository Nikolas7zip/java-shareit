package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService requestService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private final ItemRequestDto inputRequestDto = new ItemRequestDto(1L, "Молоток надо!", null, null);
    private final ItemRequestDto outputRequestDto = new ItemRequestDto(1L, "Молоток надо!",
            LocalDateTime.now().withNano(0), new ArrayList<>());
    private final String requesterId = "3";

    @Test
    void shouldCreateRequest() throws Exception {
        when(requestService.create(anyLong(), any()))
                .thenReturn(outputRequestDto);

        mockMvc.perform(post("/requests")
                                .header("X-Sharer-User-Id", requesterId)
                                .content(mapper.writeValueAsString(inputRequestDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(outputRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(outputRequestDto.getCreated().toString())));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestWithBlankDescription() throws Exception {
        ItemRequestDto requestDtoFailDescription = new ItemRequestDto(3L, "  ", null, null);
        when(requestService.create(anyLong(), any()))
                .thenReturn(null);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", requesterId)
                        .content(mapper.writeValueAsString(requestDtoFailDescription))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnRequest() throws Exception {
        when(requestService.get(anyLong(), anyLong()))
                .thenReturn(outputRequestDto);

        mockMvc.perform(get("/requests/{requestId}", outputRequestDto.getId())
                        .header("X-Sharer-User-Id", requesterId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(outputRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(outputRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(outputRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void shouldReturnUserRequests() throws Exception {
        when(requestService.getByRequester(anyLong()))
                .thenReturn(List.of(outputRequestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", requesterId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(outputRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(outputRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(outputRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].items", hasSize(0)));
    }

    @Test
    void shouldReturnRequestsOfOtherUsers() throws Exception {
        ItemRequestDto requestWithItemsDto = new ItemRequestDto(2L, "Ищу грузовик для переезда",
                LocalDateTime.now().withNano(0), new ArrayList<>());
        ItemDto itemOnRequest = new ItemDto(4L, "Грузовик", "Грузовик для перевозок", true);
        itemOnRequest.setRequestId(requestWithItemsDto.getId());
        requestWithItemsDto.setItems(List.of(itemOnRequest));

        when(requestService.getOfOtherUsers(anyLong(), any()))
                .thenReturn(List.of(requestWithItemsDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", requesterId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestWithItemsDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestWithItemsDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(requestWithItemsDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(itemOnRequest.getId()), Long.class))
                .andExpect(jsonPath("$[0].items[0].name", is(itemOnRequest.getName())))
                .andExpect(jsonPath("$[0].items[0].description", is(itemOnRequest.getDescription())))
                .andExpect(jsonPath("$[0].items[0].available", is(itemOnRequest.getAvailable())))
                .andExpect(jsonPath("$[0].items[0].requestId", is(requestWithItemsDto.getId()), Long.class));
    }

    @Test
    void shouldReturnBadRequestWhenGetRequestsWithWrongPageSize() throws Exception {
        when(requestService.getOfOtherUsers(anyLong(), any()))
                .thenReturn(null);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", requesterId)
                        .param("from", "0")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenGetRequestsWithWrongFromIndex() throws Exception {
        when(requestService.getOfOtherUsers(anyLong(), any()))
                .thenReturn(null);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", requesterId)
                        .param("from", "-1")
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
