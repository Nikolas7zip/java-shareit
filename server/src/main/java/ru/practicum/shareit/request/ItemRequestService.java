package ru.practicum.shareit.request;

import ru.practicum.shareit.pagination.EntityPagination;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestDto requestDto);

    ItemRequestDto get(Long userId, Long id);

    List<ItemRequestDto> getByRequester(Long userId);

    List<ItemRequestDto> getOfOtherUsers(Long userId, EntityPagination pagination);
}
