package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.EntityPagination;
import ru.practicum.shareit.request.dto.ItemRequestDto;
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
public class ItemRequestServiceTest {

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private ItemRepository mockItemRepository;

    @Mock
    private ItemRequestRepository mockRequestRepository;

    private ItemRequestService requestService;
    private ItemRequest itemRequest;
    private ItemRequestDto requestDto = new ItemRequestDto(1L, "Лопату надо", LocalDateTime.now().withNano(0),
            new ArrayList<>());
    private User requester;

    @BeforeEach
    void setUp() {
        requestService = new ItemRequestServiceImpl(
                mockUserRepository,
                mockRequestRepository,
                mockItemRepository
        );

        requester = new User();
        requester.setId(2L);
        requester.setName("Admin");
        requester.setEmail("admin@mail.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(requestDto.getId());
        itemRequest.setDescription(requestDto.getDescription());
        itemRequest.setCreated(requestDto.getCreated());
        itemRequest.setRequesterId(requester.getId());
    }

    @Test
    void shouldCreateRequest() {
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(mockRequestRepository.save(any())).thenReturn(itemRequest);

        ItemRequestDto createdRequest = requestService.create(requester.getId(), requestDto);
        assertEquals(requestDto, createdRequest);
    }

    @Test
    void shouldFindRequest() {
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(mockRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(mockItemRepository.findAllByRequestId(anyLong())).thenReturn(new ArrayList<>());

        ItemRequestDto findRequest = requestService.get(requester.getId(), itemRequest.getId());
        assertEquals(requestDto, findRequest);
    }

    @Test
    void shouldThrowWhenNotFoundRequest() {
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(mockRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> requestService.get(requester.getId(), 10L)
        );
    }

    @Test
    void shouldFindRequesterRequests() {
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(mockRequestRepository.findAllByRequesterId(anyLong(), any())).thenReturn(List.of(itemRequest));
        when(mockItemRepository.findAllByRequestIdIn(any())).thenReturn(new ArrayList<>());

        List<ItemRequestDto> requestDtos = requestService.getByRequester(requester.getId());
        assertEquals(List.of(requestDto), requestDtos);
    }

    @Test
    void shouldReturnEmptyRequests() {
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(mockRequestRepository.findAllByRequesterId(anyLong(), any())).thenReturn(new ArrayList<>());

        List<ItemRequestDto> requestDtos = requestService.getByRequester(requester.getId());
        assertTrue(requestDtos.isEmpty());
    }

    @Test
    void shouldThrowWhenRequesterNotFound() {
        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> requestService.getByRequester(10L)
        );
    }

    @Test
    void shouldFindRequestWithItems() {
        ItemRequest requestTool = new ItemRequest();
        requestTool.setId(4L);
        requestTool.setDescription("Что-то для сельскохозяйственных работ");
        requestTool.setCreated(LocalDateTime.now().withNano(0));
        requestTool.setRequesterId(requester.getId());

        Item itemOnRequest = new Item();
        itemOnRequest.setId(6L);
        itemOnRequest.setName("Вроде лопата");
        itemOnRequest.setDescription("Лопату ннада?");
        itemOnRequest.setAvailable(true);
        itemOnRequest.setRequestId(requestTool.getId());

        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(mockRequestRepository.findAllByRequesterId(anyLong(), any())).thenReturn(List.of(requestTool));
        when(mockItemRepository.findAllByRequestIdIn(any())).thenReturn(List.of(itemOnRequest));

        List<ItemRequestDto> requestDtos = requestService.getByRequester(requester.getId());
        ItemRequestDto expectedRequest = new ItemRequestDto(requestTool.getId(), requestTool.getDescription(),
                requestTool.getCreated(), ItemMapper.mapToItemDto(List.of(itemOnRequest)));

        assertEquals(List.of(expectedRequest), requestDtos);

    }

    @Test
    void shouldFindRequestsOtherUsers() {
        User userWithInterest = new User();
        userWithInterest.setId(5L);
        userWithInterest.setName("Ivan First");
        userWithInterest.setEmail("ivan@mail.com");

        when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(userWithInterest));
        when(mockRequestRepository.findByRequesterIdNot(anyLong(), any())).thenReturn(new PageImpl<>(List.of(itemRequest)));
        when(mockItemRepository.findAllByRequestIdIn(any())).thenReturn(new ArrayList<>());

        List<ItemRequestDto> requestDtos = requestService.getOfOtherUsers(userWithInterest.getId(),
                EntityPagination.of(0, 10));
        assertEquals(List.of(requestDto), requestDtos);
    }
}
