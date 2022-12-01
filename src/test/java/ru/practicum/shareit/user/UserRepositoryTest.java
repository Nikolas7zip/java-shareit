package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

@Transactional
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        User user = new User();
        user.setName("Tester");
        user.setEmail("test@mail.com");
        User savedUser = userRepository.save(user);

        Assertions.assertEquals(user.getName(), savedUser.getName());
        Assertions.assertEquals(user.getEmail(), savedUser.getEmail());
    }

    @Test
    void shouldThrowWhenSaveUserWithSameMail() {
        User user1 = new User();
        user1.setName("Tester");
        user1.setEmail("test_m@mail.com");
        User user2 = new User();
        user2.setName("Master");
        user2.setEmail("test_m@mail.com");

        final Exception ex = Assertions.assertThrows(
                Exception.class,
                () -> {
                    userRepository.save(user1);
                    userRepository.save(user2);
                }
        );
    }
}
