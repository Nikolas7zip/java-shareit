package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.ArrayList;
import java.util.List;

public class ItemRequestMapper {
    public static ItemRequest mapToNewRequest(ItemRequestDto requestDto, Long userId) {
        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequesterId(userId);
        return request;
    }

    public static ItemRequestDto mapToRequestDto(ItemRequest request) {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(request.getId());
        requestDto.setDescription(request.getDescription());
        requestDto.setCreated(request.getCreated());
        return requestDto;
    }

    public static List<ItemRequestDto> mapToRequestDto(List<ItemRequest> requests) {
        List<ItemRequestDto> dtos = new ArrayList<>();
        for (ItemRequest request : requests) {
            dtos.add(mapToRequestDto(request));
        }
        return dtos;
    }
}
