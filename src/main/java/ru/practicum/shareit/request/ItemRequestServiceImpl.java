package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.EntityPagination;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;

    private final ItemRequestRepository requestRepository;

    private final ItemRepository itemRepository;

    @Autowired
    public ItemRequestServiceImpl(UserRepository userRepository,
                                  ItemRequestRepository requestRepository,
                                  ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto requestDto) {
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        ItemRequest request = requestRepository.save(ItemRequestMapper.mapToNewRequest(requestDto, userId));
        log.info("Created " + request);

        return ItemRequestMapper.mapToRequestDto(request);
    }

    @Override
    public ItemRequestDto get(Long userId, Long id) {
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(User.class, userId));

        ItemRequest request = requestRepository.findById(id)
                                                .orElseThrow(() -> new EntityNotFoundException(ItemRequest.class, id));
        ItemRequestDto requestDto = ItemRequestMapper.mapToRequestDto(request);
        List<Item> itemsByRequest = itemRepository.findAllByRequestId(request.getId());
        requestDto.setItems(ItemMapper.mapToItemDto(itemsByRequest));
        return requestDto;
    }

    @Override
    public List<ItemRequestDto> getByRequester(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(User.class, userId));
        List<ItemRequest> requests = requestRepository.findAllByRequesterId(userId, Sort.by("created").descending());
        List<ItemRequestDto> dtos = ItemRequestMapper.mapToRequestDto(requests);
        fillRequestsDtoWithItemsData(dtos);
        return dtos;
    }

    @Override
    public List<ItemRequestDto> getOfOtherUsers(Long userId, EntityPagination pagination) {
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(User.class, userId));
        Pageable sortPage = PageRequest.of(pagination.getPage(), pagination.getSize(), Sort.by("created").descending());
        Page<ItemRequest> page = requestRepository.findByRequesterIdNot(userId, sortPage);
        List<ItemRequestDto> dtos = ItemRequestMapper.mapToRequestDto(page.getContent());
        fillRequestsDtoWithItemsData(dtos);
        return dtos;
    }

    private void fillRequestsDtoWithItemsData(List<ItemRequestDto> dtos) {
        if (!dtos.isEmpty()) {
            Map<Long, Integer> mapRequestSearch = IntStream.range(0, dtos.size()).boxed()
                    .collect(Collectors.toMap(i -> dtos.get(i).getId(), i -> i));
            List<Item> items = itemRepository.findAllByRequestIdIn(mapRequestSearch.keySet());
            for (Item item : items) {
                Long requestId = item.getRequestId();
                ItemDto itemDto = ItemMapper.mapToItemDto(item);
                dtos.get(mapRequestSearch.get(requestId)).getItems().add(itemDto);
            }
        }
    }

}
