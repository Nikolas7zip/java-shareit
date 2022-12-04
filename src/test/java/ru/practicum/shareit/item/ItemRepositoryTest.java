package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DataJpaTest
public class ItemRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void shouldFindAvailableItemsByText() {
        User user1 = new User();
        user1.setName("Tester");
        user1.setEmail("test@mail.com");

        User user2 = new User();
        user2.setName("Master");
        user2.setEmail("master@mail.com");

        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        Item item1 = new Item();
        item1.setName("Лопата");
        item1.setDescription("Для огорода");
        item1.setAvailable(true);
        item1.setOwnerId(savedUser1.getId());

        Item item2 = new Item();
        item2.setName("Инструмент");
        item2.setDescription("Можно копать как лопатой");
        item2.setAvailable(false);
        item2.setOwnerId(savedUser1.getId());

        Item item3 = new Item();
        item3.setName("Набор для хозяйства");
        item3.setDescription("Лопата, грабли, тачка и др.");
        item3.setAvailable(true);
        item3.setOwnerId(savedUser1.getId());

        Item savedItem1 = itemRepository.save(item1);
        Item savedItem2 = itemRepository.save(item2);
        Item savedItem3 = itemRepository.save(item3);

        Page<Item> pageItems = itemRepository.findAvailableToRentByText("Лопата", PageRequest.of(0, 10));
        List<Item> items = pageItems.getContent();

        Assertions.assertEquals(2, items.size());
        Assertions.assertEquals(savedItem1.getId(), items.get(0).getId());
        Assertions.assertEquals(savedItem3.getId(), items.get(1).getId());
    }
}
