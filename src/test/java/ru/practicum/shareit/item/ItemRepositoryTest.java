package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Transactional
@DataJpaTest
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @Sql("/create_entities.sql")
    void shouldFindAvailableItemsByText() {
        Page<Item> pageItems = itemRepository.findAvailableToRentByText("Лопата", PageRequest.of(0, 10));
        List<Item> items = pageItems.getContent();

        Assertions.assertEquals(items.get(0).getId(), 1L);
        Assertions.assertEquals(items.get(1).getId(), 3L);
    }
}
